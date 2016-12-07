package fact.auxservice;

import com.google.common.cache.*;
import com.google.common.collect.HashBasedTable;
import fact.auxservice.strategies.AuxPointStrategy;

import fact.io.zfits.ZFitsStream;
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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

/**
 * This implements an AuxiliaryService {@link fact.auxservice.AuxiliaryService}  providing data from
 * the auxiliary files written by the telescopes data acquisition system.
 *
 * Given the path to the aux folder, that is the folder containing all the auxiliary files for FACT.
 * This service will read the requested data and store them in a map of {@link fact.auxservice.AuxPoint}.
 *
 * Only aux files newer than 2012 are supported!
 *
 * Simply provide the 'auxFolder' url to the basepath of the aux files e.g. '/fact/aux/'
 *
 * Created by kaibrugge on 07.10.14.
 */
public class AuxFileService implements AuxiliaryService {

    private Logger log = LoggerFactory.getLogger(AuxFileService.class);

    @Parameter(required = true, description = "The url pointing to the path containing a the auxilary " +
            "data in FACTS canonical folder structure." )
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

        if(eventTimeStamp.isAfterNow()){
            log.warn("The requested timestamp seems to be in the future.");
        }
        try {
            AuxCache.CacheKey key = new AuxCache().new CacheKey(serviceName, eventTimeStamp);

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


    /**
     * This method returns an AuxPoints for a whole night according to the strategy and the time stamp passed to this method.
     *
     * @throws IOException when no auxpoint can be found for given night
     */
    public synchronized SortedSet<AuxPoint> getAuxiliaryDataForWholeNight(AuxiliaryServiceName serviceName, DateTime night) throws IOException {
        try {
            AuxCache.CacheKey key = new AuxCache().new CacheKey(serviceName, night);

            TreeSet<AuxPoint> auxPoints = cache.get(key);
            if (auxPoints.isEmpty()){
                throw new IOException("No auxpoints found for the given night " + night);
            }
            return auxPoints;

        } catch (ExecutionException e) {
            throw new IOException("No auxpoints found for the given night" + night);
        }
    }

    private TreeSet<AuxPoint>  readDataFromFile(AuxCache.CacheKey key) throws Exception {
        Path pathToFile = Paths.get(auxFolder.getPath(), key.path.toString());
        if(pathToFile == null){
            log.error("Could not load auxfile {} for night {}", key.service, key.factNight);
            throw new IOException("Could not load auxfile for key " +  key);
        }
        TreeSet<AuxPoint> result = new TreeSet<>();
        ZFitsStream stream = new ZFitsStream(new SourceURL(pathToFile.toUri().toURL()));
        stream.setTableName(key.service.name());
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
            throw new RuntimeException(e);
        }
    }


    @Override
    public void reset() throws Exception {
    }

}
