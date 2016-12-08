package fact;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import stream.annotations.Parameter;
import stream.io.SourceURL;
import stream.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by mackaiver on 08/12/16.
 */
public class DrsFileService implements Service {

    private Logger log = LoggerFactory.getLogger(DrsFileService.class);

    @Parameter(required = true, description = "The url pointing to the path containing the FACT raw data " +
            "in FACTS canonical folder structure." )
    public SourceURL rawDataFolder;
    public void setAuxFolder(SourceURL rawDataFolder) {
        this.rawDataFolder = rawDataFolder;
    }

    private LoadingCache<DrsCacheKey, CalibrationInfo> cache = CacheBuilder.newBuilder()
            .maximumSize(15)
            .expireAfterAccess(20, TimeUnit.MINUTES)
            .removalListener(notification -> log.debug("Removing Data from cache for cause {}", notification.getCause()))
            .build(new CacheLoader<DrsCacheKey, CalibrationInfo>() {
                @Override
                public CalibrationInfo load(DrsCacheKey key) throws Exception {
                    Path p = findDrsFile(key);
                    return readDrsInfos(p);
                }
            });

    private Path findDrsFile(DrsCacheKey key) throws IOException {
        Path pathToFolder = Paths.get(rawDataFolder.getPath(),  key.partialPathToFolder.toString());

        return Files
                .list(pathToFolder)
                .filter(p -> p.endsWith(".drs.fits.gz"))
                .min((lhs, rhs) -> getObservationDate(lhs).compareTo(key.observationDate))
                .orElseThrow(IOException::new);

    }

    private LocalDateTime getObservationDate(Path p){
        return LocalDateTime.now();
    }
    private CalibrationInfo readDrsInfos(Path p){
        return null;
    }

    private class DrsCacheKey {
        final int runId;

        final int month;
        final int day;
        final int year;
        final LocalDateTime observationDate;
        final Path partialPathToFolder;

        DrsCacheKey(LocalDateTime dateTime, int runId) {
            month = dateTime.getMonthValue();
            day = dateTime.getDayOfMonth();
            year = dateTime.getYear();
            partialPathToFolder = dateTimeStampToFACTPath(dateTime);
            observationDate = dateTime;
            this.runId = runId;

        }

//        /**
//         * Takes a year, month, day and returns the partial canonical path.
//         * For example 2016-01-03 09:30:12 returns a path to "2016/01/02" while
//         * 2016-01-03 13:30:12 would return "2016/01/03"
//         *
//         * @param year the year
//         * @param month the month of year {1-12}
//         * @param day the day of the month {1-31}
//         * @return a partial path starting with the year.
//         */
//        public Path dateTimeStampToFACTPath(int year, int month, int day){
//            return Paths.get(String.format("%04d", year), String.format("%02d",month), String.format("%02d", day));
//        }
    }


    public class CalibrationInfo{
        final float[] drsBaselineMean;
        final float[] drsGainMean;
        final float[] drsTriggerOffsetMean;
        final LocalDateTime timeOfCalibration;

        public CalibrationInfo(LocalDateTime timeOfCalibration,
                               float[] drsBaselineMean,
                               float[] drsGainMean,
                               float[] drsTriggerOffsetMean)
        {
            this.drsBaselineMean = drsBaselineMean;
            this.drsGainMean = drsGainMean;
            this.drsTriggerOffsetMean = drsTriggerOffsetMean;
            this.timeOfCalibration = timeOfCalibration;
        }
    }


    /**
     * Takes a dateTime object and returns the canonical path to an aux or data file.
     * For example 2016-01-03 09:30:12 returns a path to "2016/01/02" while
     * 2016-01-03 13:30:12 would return "2016/01/03"
     *
     * @param timeStamp the timestamp to get the night for
     * @return a partial path starting with the year.
     */
    public Path dateTimeStampToFACTPath(LocalDateTime timeStamp){
        LocalDateTime offsetDate = timeStamp.minusHours(12);
        int year = offsetDate.getYear();
        int month = offsetDate.getMonthValue();
        int day = offsetDate.getDayOfMonth();

        return Paths.get(String.format("%04d", year), String.format("%02d",month), String.format("%02d", day));
    }



    @Override
    public void reset() throws Exception {

    }
}
