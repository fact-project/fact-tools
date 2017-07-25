package fact;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import fact.io.hdureader.*;
import stream.Data;
import stream.annotations.Parameter;
import stream.io.SourceURL;
import stream.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 *
 * This is a service which, provided a data item from the telescopes data stream,
 * this returns the proper drs calibration constants.
 * By 'proper' I mean the drs constants which have been taken closest in time to the run the event was taken from.
 * The url should point to the usual raw data folder containing the canonical file structure e.g. /fact/raw on the isdc.
 *
 *
 * Created by mackaiver on 08/12/16.
 */
public class DrsFileService implements Service {

    private Logger log = LoggerFactory.getLogger(DrsFileService.class);

    @Parameter(required = true, description = "The url pointing to the path containing the FACT raw data " +
            "in FACTS canonical folder structure." )
    private SourceURL rawDataFolder;
    public void setRawDataFolder(SourceURL rawDataFolder) {
        this.rawDataFolder = rawDataFolder;
    }

    private LoadingCache<DrsCacheKey, CalibrationInfo> cache = CacheBuilder.newBuilder()
            .maximumSize(15)
            .expireAfterAccess(20, TimeUnit.MINUTES)
            .removalListener(notification -> log.debug("Removing Data from cache for cause {}", notification.getCause()))
            .build(new CacheLoader<DrsCacheKey, CalibrationInfo>() {
                @Override
                public CalibrationInfo load(DrsCacheKey key) throws Exception {
                    return readDrsInfos(key);
                }
            });


    /**
     * This method returns the calibration info for the given data item. This item should contain a key
     * called DATE-OBS. Which is the beginning of the observation. If the drs calibration constants have not been loaded
     * into the cache, all drs files in the folder of the current run are parsed for their date. The constants
     * in the file closest to DATE-OBS are read and returned here.
     *
     * @param item a data item with FACT data
     * @return a CalibrationInfo instance which holds the calibration constants
     * @throws IOException if no file with drs constant can be found or an error occurs while reading it.
     */
    public CalibrationInfo getCalibrationConstantsForDataItem(Data item) throws IOException {
        LocalDateTime observationDate = LocalDateTime.parse(item.get("DATE-OBS").toString());
        int runId = (int) item.get("RUNID");
        try {
            return cache.get(new DrsCacheKey(observationDate, runId));
        } catch (ExecutionException e) {
            throw new IOException("Could not load DRS data into cache. " + e.getMessage());
        }
    }


    private LocalDateTime getObservationDate(Path p) {
        FITS fits = FITS.fromPath(p);
        HDU hdu = fits.getHDU("DrsCalibration");
        return hdu.header.date().orElse(LocalDateTime.MIN);
    }


    private CalibrationInfo readDrsInfos(DrsCacheKey key) throws IOException {
        Path pathToFolder = Paths.get(rawDataFolder.getPath(),  key.partialPathToFolder.toString());

        //find drsfile closest in time to the observationdate
        Path pathToClosestDrsFile = Files
                .list(pathToFolder)
                .filter(p -> p.getFileName().toString().contains(".drs.fits"))
                .reduce((a, b) -> {
                    long toA = ChronoUnit.SECONDS.between(getObservationDate(a), key.observationDate);
                    long toB = ChronoUnit.SECONDS.between(getObservationDate(b), key.observationDate);
                    return Math.abs(toA) < Math.abs(toB) ? a : b;
                })
                .orElseThrow(()-> new IOException("No files found matching *.drs.fits* or no dates in FITS header."));

        log.info("Loading DRS file {} for run id {}", pathToClosestDrsFile, key.runId);
        FITS fits = new FITS(pathToClosestDrsFile.toUri().toURL());
        HDU calibrationHDU = fits.getHDU("DrsCalibration");
        BinTable binTable = calibrationHDU.getBinTable();

        OptionalTypesMap<String, Serializable> row = BinTableReader.forBinTable(binTable).getNextRow();

        //get the arrays needed for calibration. if one is missing throw an exception.
        float[] baselineMean = row.getFloatArray("BaselineMean")
                .orElseThrow(()-> new IOException("BaseLineMean not found in BinTable"));

        float[] triggerOffsetMean = row.getFloatArray("TriggerOffsetMean")
                .orElseThrow(()-> new IOException("TriggerOffsetMean not found in BinTable"));

        float[] gainMean = row.getFloatArray("GainMean")
                .orElseThrow(()-> new IOException("GainMean not found in BinTable"));

        LocalDateTime calibrationDateTime = calibrationHDU.header.date()
                .orElseThrow(()-> new IOException("Date of calibration not found in BinTable"));

        return new CalibrationInfo(calibrationDateTime, baselineMean, gainMean, triggerOffsetMean);
    }

    private class DrsCacheKey {
        final int runId;

        final int month;
        final int day;
        final int year;
        final LocalDateTime observationDate;
        final Path partialPathToFolder;

        DrsCacheKey(LocalDateTime observationDateTime, int runId) {
            month = observationDateTime.getMonthValue();
            day = observationDateTime.getDayOfMonth();
            year = observationDateTime.getYear();
            partialPathToFolder = dateTimeStampToFACTPath(observationDateTime);
            observationDate = observationDateTime;
            this.runId = runId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DrsCacheKey that = (DrsCacheKey) o;

            return runId == that.runId && month == that.month && day == that.day && year == that.year;

        }

        @Override
        public int hashCode() {
            int result = runId;
            result = 31 * result + month;
            result = 31 * result + day;
            result = 31 * result + year;
            return result;
        }

        /**
         * Takes a dateTime object and returns the canonical path to an aux or data file.
         * For example 2016-01-03 09:30:12 returns a path to "2016/01/02" while
         * 2016-01-03 13:30:12 would return "2016/01/03"
         *
         * @param timeStamp the timestamp to get the night for
         * @return a partial path starting with the year.
         */
        private Path dateTimeStampToFACTPath(LocalDateTime timeStamp){
            LocalDateTime offsetDate = timeStamp.minusHours(12);
            int year = offsetDate.getYear();
            int month = offsetDate.getMonthValue();
            int day = offsetDate.getDayOfMonth();

            return Paths.get(String.format("%04d", year), String.format("%02d",month), String.format("%02d", day));
        }
    }

    /**
     * A class to hold the calibrations constants needed to apply the infamous drs calibration
     * to FACT raw data.
     */
    public class CalibrationInfo{
        public final float[] drsBaselineMean;
        public final float[] drsGainMean;
        public final float[] drsTriggerOffsetMean;
        public final LocalDateTime timeOfCalibration;

        CalibrationInfo(LocalDateTime timeOfCalibration,
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






    @Override
    public void reset() throws Exception {

    }
}
