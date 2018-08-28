package fact.auxservice;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import fact.auxservice.strategies.AuxPointStrategy;
import fact.io.hdureader.BinTable;
import fact.io.hdureader.BinTableReader;
import fact.io.hdureader.FITS;
import fact.io.hdureader.OptionalTypesMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * This implements an AuxiliaryService {@link fact.auxservice.AuxiliaryService}  providing data from
 * the auxiliary files written by the telescopes data acquisition system.
 * <p>
 * Given the path to the aux folder, that is the folder containing all the auxiliary files for FACT.
 * This service will read the requested data and store them in a map of {@link fact.auxservice.AuxPoint}.
 * <p>
 * Only aux files newer than 2012 are supported!
 * <p>
 * Simply provide the 'auxFolder' url to the basepath of the aux files e.g. '/fact/aux/'
 * <p>
 * Optionally you can also provide the path to the folder containing the aux data for that specific night e.g. '/fact/aux/2013/01/02/'
 * <p>
 * Because one might want read from multiple sources at once, and many streams are accessing this service at once, or
 * one simply wants to access many different aux files the data from one file is cached into a guava cache.
 * This saves us the overhead of keeping tracks of different files in some custom structure.
 * <p>
 * Created by kaibrugge on 07.10.14.
 */
public class AuxFileService implements AuxiliaryService {

    private Logger log = LoggerFactory.getLogger(AuxFileService.class);

    @Parameter(required = true, description = "The url pointing to the path containing a the auxilary " +
            "data in FACTS canonical folder structure.")
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
     * <p>
     * AuxPoint trackingPoint = auxService.getAuxiliaryData(AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION, timeStamp, closest);
     * double ra = trackingPoint.getDouble("Ra");
     *
     * @param serviceName    The name of the service.
     * @param eventTimeStamp The time stamp of the current raw data event.
     * @param strategy       One of the strategies provided.
     * @return the auxpoint selected by the strategy if it exists.
     * @throws IOException when no auxpoint can be found for given timestamp
     */
    @Override
    public synchronized AuxPoint getAuxiliaryData(AuxiliaryServiceName serviceName, ZonedDateTime eventTimeStamp, AuxPointStrategy strategy) throws IOException {
        if (eventTimeStamp.isAfter(ZonedDateTime.now())) {
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
     * This method returns all AuxPoints for the whole night given by the 'night' timestamp.
     *
     * @throws IOException when no auxpoint can be found for the given night
     */
    public synchronized SortedSet<AuxPoint> getAuxiliaryDataForWholeNight(AuxiliaryServiceName serviceName, ZonedDateTime night) throws IOException {
        try {
            AuxCache.CacheKey key = new AuxCache().new CacheKey(serviceName, night);

            TreeSet<AuxPoint> auxPoints = cache.get(key);
            if (auxPoints.isEmpty()) {
                throw new IOException("No auxpoints found for the given night " + night);
            }
            return auxPoints;

        } catch (ExecutionException e) {
            throw new IOException("No auxpoints found for the given night" + night);
        }
    }

    private TreeSet<AuxPoint> readDataFromFile(AuxCache.CacheKey key) throws Exception {
        TreeSet<AuxPoint> result = new TreeSet<>();

        Path pathToFile = Paths.get(auxFolder.getPath(), key.path.toString());

        if (pathToFile == null) {
            log.error("Could not load aux file {} for night {}", key.service, key.factNight);
            throw new IOException("Could not load aux file for key " + key);
        }

        //test whether file is in current directory. this ensures compatibility to fact-tools version < 18.0
        if (!pathToFile.toFile().canRead()) {
            pathToFile = Paths.get(auxFolder.getPath(), key.filename);

            if (!pathToFile.toFile().canRead()) {
                log.error("Could not load aux file in given directory {}", auxFolder);
            }
        }


        FITS fits = FITS.fromPath(pathToFile);
        String extName = key.service.name();

        BinTable auxDataBinTable = fits.getBinTableByName(extName)
                .orElseThrow(
                        () -> new RuntimeException("BinTable '" + extName + "' not in aux file")
                );
        BinTableReader auxDataBinTableReader = BinTableReader.forBinTable(auxDataBinTable);

        while (auxDataBinTableReader.hasNext()) {
            OptionalTypesMap<String, Serializable> auxData = auxDataBinTableReader.getNextRow();

            auxData.getDouble("Time").ifPresent(time -> {
                long value = (long) (time * 24 * 60 * 60 * 1000);
                ZonedDateTime t = Instant.ofEpochMilli(value).atZone(ZoneOffset.UTC);
                AuxPoint p = new AuxPoint(t, auxData);
                result.add(p);
            });

        }
        return result;
    }


    @Override
    public void reset() throws Exception {
    }

}
