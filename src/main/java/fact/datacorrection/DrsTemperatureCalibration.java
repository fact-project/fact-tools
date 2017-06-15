package fact.datacorrection;

import com.google.common.collect.ImmutableMap;
import fact.Utils;
import fact.auxservice.AuxPoint;
import fact.auxservice.AuxiliaryService;
import fact.auxservice.AuxiliaryServiceName;
import fact.auxservice.strategies.AuxPointStrategy;
import fact.auxservice.strategies.Closest;
import fact.io.hdureader.BinTable;
import fact.io.hdureader.BinTableReader;
import fact.io.hdureader.FITS;
import fact.io.hdureader.HDU;

import fact.io.zfits.ZFitsStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.annotations.Service;
import stream.io.SourceURL;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by florian on 06.12.16.
 */
public class DrsTemperatureCalibration implements StatefulProcessor {

    private final static Logger log = LoggerFactory.getLogger(DrsTemperatureCalibration.class);

    /******************************************************************************************************************/
    @Parameter(required = false, description = "Key to the Data array to be calibrated", defaultValue = "Data")
    private String dataKey = "Data";

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    @Parameter(required = false, description = "Key to the Calibrated-Data array", defaultValue = "DataCalibrated")
    private String outputKey = "DataCalibrated";

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    /******************************************************************************************************************/
    @Parameter(required = true, description = "A URL to the DRS-FitValue data (in FITS formats)",
               defaultValue = "Null. Will try to find path to DRS-FitValueFile from the stream.")
    private SourceURL drsFitFilePath = null;

    public void setDrsFitFilePath(SourceURL drsFitFilePath) { this.drsFitFilePath = drsFitFilePath;
    }

    /******************************************************************************************************************/
    @Service(required = true, description = "Name of the service that provides aux files")
    private AuxiliaryService auxService;

    public void setAuxService(AuxiliaryService auxService) {
        this.auxService = auxService;
    }

    private final static AuxPointStrategy auxPointStrategy = new Closest();

    /******************************************************************************************************************/
    // The following keys are required to exist in the DRS-FitValue data
    private final static String[] fitParameterKeys = new String[]{"BaselineSlope", "BaselineOffset",
                                                                  "GainSlope", "GainOffset"};

    private HashMap<ZonedDateTime, HashMap<String, float[]>> intervalFitParameterMap = new HashMap();

    /******************************************************************************************************************/
    // The following keys are required to exist in the raw data
    private final static String[] dataKeys = new String[]{"UnixTimeUTC", "NPIX", "NCELLS", "NROI"};

    /******************************************************************************************************************/
    /* // new version -dosent work jet
    @Override
    public void init(ProcessContext processContext) throws Exception {
        log.debug("----- init -----");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
        if (this.drsFitFilePath != null) {
            try {
                FITS f = FITS.fromFile(new File(this.drsFitFilePath.getFile()));
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
                log.error("Could not load file '"+this.drsFitFilePath+"' specified in the url_fitValueFile.");
                throw new RuntimeException(e.getMessage());
            }
        } else {
            log.error("drsFitFilePath == null");
        }
    }

    /******************************************************************************************************************/
    /*
    private  void updateDrsFitParameterMap(BinTable intervalData, ZonedDateTime dateTime)
    {
        log.debug("----- updateDrsFitParameterMap -----");
        try {
            BinTableReader reader = BinTableReader.forBinTable(intervalData);
            //first row contains null stuff
            Map<String, Serializable> row = reader.getNextRow();

            // this for-loop is simply a check that fires an exception if any
            // of the expected keys is missing in table
            //Utils.mapContainsKeys(intervalData, this.fitParameterKeys); //TODO fix

            HashMap<String, float[]> intervalFitParameter = new HashMap();
            for (String key: this.fitParameterKeys) {
                intervalFitParameter.put(key, (float[]) row.get(key));
                log.debug("{} data: {}", key, intervalFitParameter.get(key));
            }
            this.intervalFitParameterMap.put(dateTime, intervalFitParameter);
        } catch (Exception e) {

            log.error("Failed to load DRS data: {}", e.getMessage());
            if (log.isDebugEnabled())
                e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }


    //old stuff TODO remove
    /******************************************************************************************************************/
    @Override
    public void init(ProcessContext processContext) throws Exception {
        log.debug("----- init -----");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
        if (this.drsFitFilePath != null) {
            try {
                String[] dateStringCollection = {"2013-06-07 12", "2014-05-20 12", "2015-05-26 12"};
                final ZFitsStream stream = new ZFitsStream(drsFitFilePath);
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
                log.error("Could not load file '"+this.drsFitFilePath+"' specified in the url_fitValueFile.");
                throw new RuntimeException(e.getMessage());
            }
        } else {
            log.error("drsFitFilePath == null");
        }
    }

    //old stuff TODO remove
    /******************************************************************************************************************/
    private  void updateDrsFitParameterMap(Data tabledata, ZonedDateTime dateTime)
    {
        log.debug("----- updateDrsFitParameterMap -----");
        try {
            // this for-loop is simply a check that fires an exception if any
            // of the expected keys is missing in table
            Utils.mapContainsKeys(tabledata, this.fitParameterKeys); //TODO fix

            HashMap<String, float[]> intervalFitParameter = new HashMap();
            for (String key: this.fitParameterKeys) {
                intervalFitParameter.put(key, (float[]) tabledata.get(key));
                log.debug("{} data: {}", key, intervalFitParameter.get(key));
            }
            this.intervalFitParameterMap.put(dateTime, intervalFitParameter);
        } catch (Exception e) {

            log.error("Failed to load DRS data: {}", e.getMessage());
            if (log.isDebugEnabled())
                e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }



    int eventCounter = 0;
    /******************************************************************************************************************/
    @Override
    public Data process(Data data) {
        log.debug("----- process -----");

        this.eventCounter ++;
        if(eventCounter % 100 == 0) {
            log.info("eventNr: {}", this.eventCounter);
        }

        Utils.mapContainsKeys(data, this.dataKeys);



        int[] unixTimeUTC = (int[]) data.get("UnixTimeUTC");
        Instant instant = Instant.ofEpochSecond(unixTimeUTC[0],unixTimeUTC[1] * 1000);
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));

        ZonedDateTime intervalDateTimeKey = null;
        for ( ZonedDateTime dateTimeKey : this.intervalFitParameterMap.keySet() ) {
            if (dateTime.compareTo(dateTimeKey) > 0) {
                intervalDateTimeKey = dateTimeKey;
                break;
            }

        }

        if(intervalDateTimeKey == null) {
            throw new IllegalArgumentException("Cant handle File: No drs fit parameters available for the drs-file datetime '"+dateTime.toString()+"'");
        }

        log.debug("Get patchTemperaure from AuxiliaryService with AuxPointStrategy {}", this.auxPointStrategy );
        AuxPoint auxPoint;
        try {
            auxPoint = this.auxService.getAuxiliaryData(AuxiliaryServiceName.FAD_CONTROL_TEMPERATURE, dateTime, this.auxPointStrategy);
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
        data.put(this.outputKey, calibrated); //TODO check move into applyDrsCalibration

        return data;
    }

    /******************************************************************************************************************/
    private double[] applyDrsCalibration(Data data,  float[] patchTemperatures, ZonedDateTime intervalDateTimeKey){
        log.debug("----- applyDrsCalibration -----");

        int pixelCount = (int) data.get("NPIX");
        int cellCount = (int) data.get("NCELLS");
        int ROI = (int) data.get("NROI");

        short[] startCellVector = (short[]) data.get("StartCellData");
        if (startCellVector == null) {
            log.error(" data .fits file did not contain startcell data. cannot apply drscalibration");
            return null;
        }

        double[] rawData = Utils.toDoubleArray(data.get(this.dataKey));
        if (rawData == null) {
            String errString = " data .fits file did not contain the value for the key "
                               + this.dataKey + ". cannot apply drsCalibration";
            log.error(errString);
            throw new RuntimeException(errString);
        }

        //TODO update text

        // We do not entirely know how the calibration constants, which are
        // saved in a filename.drs.fits file
        // were calculated, so it is not fully clear how they should be applied
        // to the raw data for calibration.
        // apparently the calibration constants were transformed to the unit mV,
        // which means we have to do the same to
        // the raw data prior to apply the calibration
        //
        // on the FAD board, there is a 12bit ADC, with a 2.0V range, so the
        // factor between ADC units and mV is
        // ADC2mV = 2000/4096. = 0.48828125 (numerically exact)
        //
        // from the schematic of the FAD we learned, that the voltage at the ADC
        // should be 1907.35 mV when the calibration DAC is set to 50000.
        //
        // One would further assume that the calibration constants are
        // calculated like this:

        // The DRS Offset of each bin in each channel is the mean value in this
        // very bin,
        // obtained from so called DRS pedestal data
        // Its value is about -1820 ADC units or -910mV

        // In order to obtain the DRS Gain of each bin of each channel
        // again data is takes, with the calibration DAC set to 50000
        // This is called DRS calibration data.
        // We assume the DRS Offset is already subtracted from the DRS
        // calibration data
        // so the typical value is assumed to be ~3600 ADC units oder ~1800mV
        // As mentioned before, the value *should* be 1907.35 mV
        // So one might assume that the Gain is a number, which actually
        // converts ~3600 ADC units into 1907.35mV for each bin of each channel.
        // So that the calibration procedure looks like this
        // TrueValue = (RawValue - Offset) * Gain
        // The numerical value of Gain would differ slightly from the
        // theoretical value of 2000/4096.
        // But this is apparently not the case.
        // The Gain, as it is stored in the DRS calibration file of FACT++ has
        // numerical values
        // around +1800.
        // So it seems one should calibrate like this:
        // TrueValue = (RawValue - Offset) / Gain * 1907.35

        // When these calibrations are done, one ends up with a quite nice
        // calibrated voltage.
        // But it turns out that, if one returns the first measurement, and
        // calculates the mean voltages
        // in each bin of the now *logical* DRS pipeline, the mean voltage is
        // not zero, but slightly varies
        // So one can store these systematical deviations from zero in the
        // logical pipeline as well, and subtract them.
        // The remaining question is, when to subtract them.
        // I assume, in the process of measuring this third calibration
        // constant, the first two
        // calibrations are already applied to the raw data.

        // So the calculation of the calibrated volatage from some raw voltage
        // works like this:
        // assume the raw voltage is the s'th sample in channel c. While the
        // Trigger made the DRS stopp in its t'th cell.
        // note, that the DRS pipeline is always 1024 bins long. This is
        // constant of the DRS4 chip.

        // TrueValue[c][s] = ( RawValue[c][s] - Offset[c][ (c+t)%1024 ] ) /
        // Gain[c][ (c+t)%1024 ] * 1907.35 - TriggerOffset[c][s]

        HashMap<String, float[]> intervalFitParameter = this.intervalFitParameterMap.get(intervalDateTimeKey);

        double dconv = 2000.0f / 4096.0f;
        double vraw;
        double[] calibrated = new double[rawData.length];
        int pos, totalPos, startCell;
        for (int pixel = 0; pixel < pixelCount; pixel++) {
            for (int slice = 0; slice < ROI; slice++) {

                startCell = 0;
                if (startCellVector[pixel] != -1) {
                    startCell = startCellVector[pixel];
                }
                pos = pixel * ROI + slice;
                totalPos = pixel * cellCount + ((startCell + slice) % (cellCount));

                vraw = rawData[pos] * dconv;
                vraw -= (intervalFitParameter.get("BaselineSlope")[totalPos] * patchTemperatures[pixel/9] + intervalFitParameter.get("BaselineOffset")[totalPos]);
                vraw /= (intervalFitParameter.get("GainSlope")[totalPos] * patchTemperatures[pixel/9] + intervalFitParameter.get("GainOffset")[totalPos]);
                vraw *= 1907.35;

                calibrated[pos] = vraw;
            }
        }
        return calibrated;
    }

    /******************************************************************************************************************/
    @Override
    public void resetState() throws Exception { }

    /******************************************************************************************************************/
    @Override
    public void finish() throws Exception { }

}
