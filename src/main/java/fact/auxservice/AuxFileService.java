package fact.auxservice;

import com.google.common.cache.*;
import com.google.common.collect.HashBasedTable;
import fact.auxservice.strategies.AuxPointStrategy;
import fact.io.FitsStream;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

/**
 * This implements an AuxiliaryService {@link fact.auxservice.AuxiliaryService}  providing data from the auxiliary
 * files written by the telescopes data acquisition system.
 *
 * Given the path to the aux folder, that is the folder containing all the auxiliary files for FACT.
 * This service will read the requested data and store them in a map of {@link fact.auxservice.AuxPoint}.
 *
 * So far only aux files newer than 2012 are supported!
 *
 * Created by kaibrugge on 07.10.14.
 */
public class AuxFileService implements AuxiliaryService {

    private Logger log = LoggerFactory.getLogger(AuxFileService.class);



    @Parameter(required = true, description = "The path to the folder containing the auxilary data as .fits files")
    public SourceURL auxFolder;
    public void setAuxFolder(SourceURL auxFolder) {
        this.auxFolder = auxFolder;
    }


    private LoadingCache<AuxCache.CacheKey, TreeSet<AuxPoint>> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(20, TimeUnit.MINUTES)
            .removalListener(notification -> log.debug("Removing Data from cache for cause {}", notification.getCause()))
            .build(new CacheLoader<AuxCache.CacheKey, TreeSet<AuxPoint>>() {
                @Override
                public TreeSet<AuxPoint> load(AuxCache.CacheKey key) throws Exception {
                    return readDataFromFile(key);
                }
            });


    private boolean isInit = false;
    private HashBasedTable<Integer, AuxiliaryServiceName, Path> auxFileUrls;

    /**
     * This method returns an AuxPoint according to the strategy and the time stamp passed to this method.
     * This is useful for getting the source position from the drive files for example. It can work like this:
     *
     *      AuxPoint trackingPoint = auxService.getAuxiliaryData(AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION, timeStamp, closest);
     *      double ra = trackingPoint.getDouble("Ra");
     *
     * @param serviceName The name of the service.
     * @param eventTimeStamp The time stamp of the current raw data event.
     * @param strategy One of the strategies provided.
     * @return the auxpoint selected by the strategy if it exists.
     * @throws IOException when no auxpoint can be found for given timestamp
     */
    @Override
    public synchronized AuxPoint getAuxiliaryData(AuxiliaryServiceName serviceName, DateTime eventTimeStamp, AuxPointStrategy strategy) throws IOException {
        if(!isInit) {
            AuxFileFinder auxFileFinder = new AuxFileFinder();
            Path p = new File(auxFolder.getPath()).toPath();
            Files.walkFileTree(p, auxFileFinder);
            auxFileUrls = auxFileFinder.auxFileTable;
            isInit = true;
        }
        if(eventTimeStamp.isAfterNow()){
            log.warn("The requested timestamp seems to be in the future.");
        }
        try {

            AuxCache.CacheKey key = new AuxCache().new CacheKey(serviceName, AuxCache.dateTimeStampToFACTNight(eventTimeStamp));

            TreeSet<AuxPoint> auxPoints = cache.get(key);
            AuxPoint pointFromTreeSet = strategy.getPointFromTreeSet(auxPoints, eventTimeStamp);
            if (pointFromTreeSet == null) {
                throw new IOException("No auxpoint found for the given timestamp " + eventTimeStamp);
            }
            return pointFromTreeSet;

        } catch (ExecutionException e) {
            throw new IOException("No auxpoint found for the given timestamp " + eventTimeStamp);
        }
    }




    private TreeSet<AuxPoint>  readDataFromFile(AuxCache.CacheKey key) throws Exception {
        Path pathToFile = auxFileUrls.get(key.factNight, key.service);
        if(pathToFile == null){
            log.error("Could not load auxfile {} for night {}", key.service, key.factNight);
            throw new IOException("Could not load auxfile for key " +  key);
        }
        TreeSet<AuxPoint> result = new TreeSet<>();
        FitsStream stream = new FitsStream(new SourceURL(pathToFile.toUri().toURL()));
        try {
            stream.init();
            Data slowData = stream.readNext();
            while (slowData != null) {
                double time = Double.parseDouble(slowData.get("Time").toString()) * 86400;// + 2440587.5;
                DateTime t = new DateTime((long)(time*1000), DateTimeZone.UTC);
                AuxPoint p = new AuxPoint(t, slowData);
                result.add(p);
                slowData = stream.readNext();
            }
            stream.close();
            return result;
        } catch (Exception e) {
            log.error("Failed to load data from AUX file: {}", e.getMessage());
            throw new RuntimeException();
        }
    }


    @Override
    public void reset() throws Exception {
    }



    /**
     * A {@code FileVisitor} that finds
     * all files that match the specified pattern for aux files
     * Auxfilenames are seperated by dots as in: date.servicename.fits
     */
    public class AuxFileFinder extends SimpleFileVisitor<Path> {

        public HashBasedTable<Integer, AuxiliaryServiceName, Path> auxFileTable = HashBasedTable.create();

        // Check file name for proper aux file naming scheme.
        void matchAuxFile(Path file) {
            try {
                Path name = file.getFileName();
                if (name != null) {
                    String[] split = name.toString().split("\\.");
                    //an aux file contains two dots.
                    if (split.length != 3) {
                        return;
                    }
                    //aux files have a fits extension
                    if(!split[2].equals("fits")){
                        return;
                    }
                    //get date string (night)
                    int night = Integer.decode(split[0]);
                    AuxiliaryServiceName serviceName = AuxiliaryServiceName.valueOf(split[1]);
                    auxFileTable.put(night, serviceName, file);
                }
            } catch(NumberFormatException e){
                log.warn("Cannot decode night from file with name: " + file.toString());
            } catch(IllegalArgumentException e){
                log.info("The file " + file + " is not a recognized auxservice");
            }
        }


        // Invoke the pattern matching
        // method on each file.
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            matchAuxFile(file);
            return CONTINUE;
        }

        //check whether the directory name only contains numbers
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            if (dir.getFileName().toString().equals("aux")){
                return CONTINUE;

            }
            try {
                short number = Short.parseShort(dir.getFileName().toString());
                if (number == 2011 || number == 2012){
                    log.info("Aux files from 2011 and 2012 are not supported");
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                log.info("Directory is not a valid aux directory with name: " + dir.toString() + ".  Skipping subtree.");
                return SKIP_SUBTREE;
            }
            return CONTINUE;
        }


        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            log.error("An error occured while trying to read directories " + exc.getLocalizedMessage() +
                    "\n Continuing with next file.");
            return CONTINUE;
        }
    }

}
