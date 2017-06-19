package fact.datacorrection;

import com.google.common.collect.ImmutableMap;
import fact.Utils;
import fact.auxservice.AuxPoint;
import fact.auxservice.AuxiliaryService;
import fact.auxservice.AuxiliaryServiceName;
import fact.auxservice.strategies.AuxPointStrategy;
import fact.auxservice.strategies.Closest;

import fact.io.zfits.ZFitsStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.annotations.Service;
import stream.io.SourceURL;

import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;


/**
 * Created by florian on 06.12.16.
 *
 * Calibrate the data by removing the temperature dependencey of the drs4 chips.
 * Therefore the data taking temperature is used do calculate for every drs4 cell
 * the temperature based data-offset and -slope. Both data-offset and slope
 * are generally given by following relationship: amplitude = a * T + b
 *
 * For historical reasons this fit-parameters are given in units of mV
 * So before we can start the calibrate we have to converts the data format
 * from adc counts to mV.
 *
 * On the FAD board, there is a 12bit ADC, with a 2.0V range, so the
 * factor between ADC units and mV is
 * adcCountsToMilliVolt = 2000/4096. = 0.48828125 (numerically exact)
 *
 * Now we have to subtract the data-offset from the data and than to divided the data by the data-slope
 *
 * From the schematic of the FAD we learned, that the voltage at the ADC
 * should be 1907.35 mV(adcMax) when the calibration DAC is set to 50000.
 *
 * So the total calibration looks like:
 * calibratedData = (rawData * adcCountsToMilliVolt - dataOffset)/dataSlope * adcMax;
 */

public class DrsTemperatureCalibration implements StatefulProcessor {

    private final static Logger log = LoggerFactory.getLogger(DrsTemperatureCalibration.class);

    private double adcCountsToMilliVolt = 2000.0 / 4096.0; //[mV/adc]
    private double adcMax = 1907.35; //[mV]

    @Parameter(description = "Key to the Data array to be calibrated", defaultValue = "Data")
    private String dataKey = "Data";

    @Parameter(description = "Key to the Calibrated-Data array", defaultValue = "DataCalibrated")
    private String outputKey = "DataCalibrated";

    @Parameter(required = true, description = "Fits file with the calibration constants",
               defaultValue = "Null. Will try to find path to fitParameterFile from the stream.")
    private SourceURL fitParameterFile = null;

    @Service(required = true, description = "Name of the service that provides aux files")
    private AuxiliaryService auxService;

    private final static AuxPointStrategy auxPointStrategy = new Closest();

    // The following keys are required to exist in the fit-parameter data
    private final static String[] fitParameterKeys = new String[]{"BaselineSlope", "BaselineOffset",
                                                                  "GainSlope", "GainOffset"};

    private HashMap<ZonedDateTime, HashMap<String, float[]>> intervalFitParameterMap = new HashMap();

    // The following keys are required to exist in the raw data
    private final static String[] dataKeys = new String[]{"UnixTimeUTC", "NPIX", "NCELLS", "NROI", "StartCellData"};

    /******************************************************************************************************************/
    /* // new version -dosent work jet
    @Override
    public void init(ProcessContext processContext) throws Exception {
        log.debug("----- init -----");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
        if (fitParameterFile != null) {
            try {
                FITS f = FITS.fromFile(new File(fitParameterFile.getFile()));
                HDU primaryHDU = f.primaryHDU;
                int nrOfIntervals =  (int) primaryHDU.header.getInt("NrOfInt").orElse(0);
                for (int intervalNr = 1; intervalNr <= nrOfIntervals; intervalNr++) {

                    String extName = "interval" + intervalNr;
                    HDU hdu = f.getHDU(extName); //.orElseThrow(IOException::new);
                    BinTable intervalData = hdu.getBinTable(); //.orElseThrow(IOException::new);
                    String dateString = hdu.header.get("LowLimit").toString();
                    ZonedDateTime dateTime = ZonedDateTime.parse(dateString, formatter);
                    updateDrsFitParameterMap(intervalData, dateTime);
                }

           } catch (Exception e) {
                log.error("Could not load file '"+fitParameterFile+"' specified in the url_fitValueFile.");
                throw new RuntimeException(e.getMessage());
            }
        } else {
            log.error("fitParameterFile == null");
        }
    }

    private  void updateDrsFitParameterMap(BinTable intervalData, ZonedDateTime dateTime)
    {
        log.debug("----- updateDrsFitParameterMap -----");
        try {
            BinTableReader reader = BinTableReader.forBinTable(intervalData);
            //first row contains null stuff
            Map<String, Serializable> row = reader.getNextRow();

            // this for-loop is simply a check that fires an exception if any
            // of the expected keys is missing in table
            //Utils.mapContainsKeys(intervalData, fitParameterKeys); //TODO fix

            HashMap<String, float[]> intervalFitParameter = new HashMap();
            for (String key: fitParameterKeys) {
                intervalFitParameter.put(key, (float[]) row.get(key));
                log.debug("{} data: {}", key, intervalFitParameter.get(key));
            }
            intervalFitParameterMap.put(dateTime, intervalFitParameter);
        } catch (Exception e) {

            log.error("Failed to load DRS data: {}", e.getMessage());
            if (log.isDebugEnabled())
                e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    /******************************************************************************************************************/
    //old stuff TODO remove
    @Override
    public void init(ProcessContext processContext) throws Exception {
        log.debug("----- init -----");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
        if (fitParameterFile != null) {
            try {
                String[] dateStringCollection = {"2013-06-07 12", "2014-05-20 12", "2015-05-26 12"}; //TODO remove magic numbers
                final ZFitsStream stream = new ZFitsStream(fitParameterFile); //TODO remove magic numbers
                int nrOfIntervals =  (int) 3;
                for (int intervalNr = 1; intervalNr <= nrOfIntervals; intervalNr++) {
                    String extName = "Interval" + String.valueOf(intervalNr);
                    stream.tableName = extName;
                    stream.init();
                    Data tabledata = stream.readNext();
                    String dateString = dateStringCollection[intervalNr-1];
                    LocalDateTime lDateTime = LocalDateTime.parse(dateString, formatter);
                    ZonedDateTime dateTime = ZonedDateTime.of(lDateTime, ZoneId.of("UTC"));
                    updateDrsFitParameterMap(tabledata, dateTime);
                }

            } catch (Exception e) {
                log.error("Could not load file '"+fitParameterFile +"' specified in the url_fitValueFile.");
                throw new RuntimeException(e.getMessage());
            }
        } else {
            log.error("fitParameterFile == null");
        }
    }

    //old stuff TODO remove
    private  void updateDrsFitParameterMap(Data tabledata, ZonedDateTime dateTime)
    {
        log.debug("----- updateDrsFitParameterMap -----");
        try {
            // this for-loop is simply a check that fires an exception if any
            // of the expected keys is missing in table
            Utils.mapContainsKeys(tabledata, fitParameterKeys); //TODO fix

            HashMap<String, float[]> intervalFitParameter = new HashMap();
            for (String key: fitParameterKeys) {
                intervalFitParameter.put(key, (float[]) tabledata.get(key));
                log.debug("{} data: {}", key, intervalFitParameter.get(key));
            }
            intervalFitParameterMap.put(dateTime, intervalFitParameter);
        } catch (Exception e) {

            log.error("Failed to load DRS data: {}", e.getMessage());
            if (log.isDebugEnabled())
                e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
    /******************************************************************************************************************/

    @Override
    public Data process(Data data) {
        log.debug("----- process -----");

        Utils.mapContainsKeys(data, dataKeys);
        Utils.mapContainsKeys(data, dataKey);

        int[] unixTimeUTC = (int[]) data.get("UnixTimeUTC");
        Instant instant = Instant.ofEpochSecond(unixTimeUTC[0],unixTimeUTC[1] * 1000);
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));

        ZonedDateTime intervalDateTimeKey = null;
        for ( ZonedDateTime dateTimeKey : intervalFitParameterMap.keySet() ) {
            if (dateTime.compareTo(dateTimeKey) > 0) {
                intervalDateTimeKey = dateTimeKey;
                break;
            }
        }

        if(intervalDateTimeKey == null) {
            throw new IllegalArgumentException("Cant handle File: No drs fit parameters available for the drs-file datetime '"+dateTime.toString()+"'");
        }

        log.debug("Get patchTemperaure from AuxiliaryService with AuxPointStrategy {}", auxPointStrategy );
        AuxPoint auxPoint;
        try {
            auxPoint = auxService.getAuxiliaryData(AuxiliaryServiceName.FAD_CONTROL_TEMPERATURE, dateTime, auxPointStrategy);
        } catch (IOException e) {
            log.error("Could not read aux data {}", e.toString());
            throw new RuntimeException(e);
        }

        ImmutableMap<String, Serializable> temperatureData = auxPoint.getData();
        float[] patchTemperatures = (float[]) temperatureData.get("temp");
        if(patchTemperatures.length != 160) { //TODO try to handle 82 Temperatures also
            throw new IllegalArgumentException("Cant handle File: There should be for every patch a Temperature. instant of '"+
                                                String.valueOf(patchTemperatures.length)+"'");
        }

        double[] calibrated = applyDrsCalibration(data, patchTemperatures, intervalDateTimeKey);
        data.put(outputKey, calibrated); //TODO check move into applyDrsCalibration

        return data;
    }

    private double[] applyDrsCalibration(Data data,  float[] patchTemperatures, ZonedDateTime intervalDateTimeKey){
        log.debug("----- applyDrsCalibration -----");

        int NRCHIDS = (int) data.get("NPIX");
        int NRCELLS = (int) data.get("NCELLS");
        int ROI = (int) data.get("NROI");

        short[] startCellVector = (short[]) data.get("StartCellData");
        // TODO check needed
        if (startCellVector == null) {
            log.error(" data .fits file did not contain startcell data. cannot apply drscalibration");
            return null;
        }

        double[] rawData = Utils.toDoubleArray(data.get(dataKey));
        // TODO check needed
        if (rawData == null) {
            String errString = " data .fits file did not contain the value for the key "
                               + dataKey + ". cannot apply drsCalibration";
            log.error(errString);
            throw new RuntimeException(errString);
        }

        HashMap<String, float[]> intervalFitParameter = intervalFitParameterMap.get(intervalDateTimeKey);

        double vraw, dataOffset, dataSlope = Double.NaN;
        double[] calibrated = new double[rawData.length];
        int startCell, sample_idx, cell_idx;
        for (int chid = 0; chid < NRCHIDS; chid++) {
            for (int sample = 0; sample < ROI; sample++) {

                startCell = Utils.sampleToCell(sample, startCellVector[chid], NRCELLS);
                sample_idx = chid * ROI + sample;
                cell_idx = chid * NRCELLS + startCell;

                dataOffset  = intervalFitParameter.get("BaselineSlope")[cell_idx] * patchTemperatures[chid/9];
                dataOffset += intervalFitParameter.get("BaselineOffset")[cell_idx];

                dataSlope  = intervalFitParameter.get("GainSlope")[cell_idx] * patchTemperatures[chid/9];
                dataSlope += intervalFitParameter.get("GainOffset")[cell_idx];

                vraw = rawData[sample_idx] * adcCountsToMilliVolt;
                vraw -= dataOffset;
                vraw /= dataSlope;
                vraw *= adcMax;

                calibrated[sample_idx] = vraw;
            }
        }
        return calibrated;
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }


    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public void setFitParameterFile(SourceURL fitParameterFile) { this.fitParameterFile = fitParameterFile; }

    public void setAuxService(AuxiliaryService auxService) {
        this.auxService = auxService;
    }
}
