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
import stream.io.SourceURL;
import java.io.IOException;
import java.io.Serializable;
import java.time.*;
import java.util.Date;


import org.joda.time.DateTime; // TODO remove when no longer needed
import org.joda.time.DateTimeZone; // TODO remove when no longer needed

/**
 * Created by florian on 06.12.16.
 */
public class DrsTemperatureCalibration implements StatefulProcessor {

    private final static Logger log = LoggerFactory.getLogger(DrsTemperatureCalibration.class);

    /******************************************************************************************************************/
    @Parameter(required = false, description = "Key to the Data array to be calibrated", defaultValue = "Data")
    private String dataKey = "Data";

    public void setDataKey(String dataKey_) {
        this.dataKey = dataKey_;
    }

    @Parameter(required = false, description = "Key to the Calibrated-Data array", defaultValue = "DataCalibrated")
    private String outputKey = "DataCalibrated";

    public void setOutputKey(String outputKey_) {
        this.outputKey = outputKey_;
    }

    /******************************************************************************************************************/
    @Parameter(required = true, description = "A URL to the DRS-FitValue data (in FITS formats)",
               defaultValue = "Null. Will try to find path to DRS-FitValueFile from the stream.")
    private SourceURL drsFitFilePath = null;

    public void setDrsFitFilePath(SourceURL drsFitFilePath_) { this.drsFitFilePath = drsFitFilePath_;
    }

    Data fitValueData = null;

    /******************************************************************************************************************/
    @Parameter(required = true, description = "Name of the service that provides aux files")
    private AuxiliaryService auxService;

    public void setAuxService(AuxiliaryService auxService) {
        this.auxService = auxService;
    }

    private final static AuxPointStrategy auxPointStrategy = new Closest();

    /******************************************************************************************************************/
    //Just for temporary storage
    private float[] baselineSlope = null;
    private float[] baselineOffset = null;
    private float[] gainSlope = null;
    private float[] gainOffset = null;

    /******************************************************************************************************************/
    // The following keys are required to exist in the DRS-FitValue data
    private final static String[] fitValueKeys = new String[]{"slope", "offset"};

    /******************************************************************************************************************/
    // The following keys are required to exist in the raw data
    private final static String[] dataKeys = new String[]{"UnixTimeUTC", "NPIX", "NCELLS", "NROI"};


    /******************************************************************************************************************/
    @Override
    public void init(ProcessContext processContext_) throws Exception {
        log.debug("----- init -----");

        if (this.drsFitFilePath != null) {
            try {
                loadDrsFitParameter(this.drsFitFilePath);
            } catch (Exception e) {
                log.error("Could not load file specified in the url_fitValueFile.");
                throw new RuntimeException(e.getMessage());
            }
        } else {
            log.error("drsFitFilePath == null");
        }
    }

    /******************************************************************************************************************/
    private  void loadDrsFitParameter(SourceURL drsFitFilePath)
    {
        log.debug("----- loadDrsFitParameter -----");
        try {
            final ZFitsStream stream = new ZFitsStream(drsFitFilePath);

            stream.tableName = "Baseline";
            stream.init();
            this.fitValueData = stream.readNext();

            log.debug("Read DRS-fitValueData data: {}", this.fitValueData);

            // this for-loop is simply a check that fires an exception if any
            // of the expected keys is missing in table 'BASELINE'

            Utils.mapContainsKeys(this.fitValueData, this.fitValueKeys);

            this.baselineSlope = ((float[]) this.fitValueData.get("slope"));
            this.baselineOffset = ((float[]) this.fitValueData.get("offset"));

            log.debug("baselineSlope data: {}", this.baselineSlope);
            log.debug("baselineOffset data: {}", this.baselineOffset);

            stream.tableName = "Gain";
            stream.init();
            this.fitValueData = stream.readNext();

            // this for-loop is simply a check that fires an exception if any
            // of the expected keys is missing in table 'GAIN'

            Utils.mapContainsKeys(this.fitValueData, this.fitValueKeys);

            this.gainSlope = ((float[]) this.fitValueData.get("slope"));
            this.gainOffset = ((float[]) this.fitValueData.get("offset"));

            log.debug("gainSlope data: {}", this.gainSlope);
            log.debug("gainOffset data: {}", this.gainOffset);

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

        log.debug("Get patchTemperaure from AuxiliaryService with AuxPointStrategy {}", this.auxPointStrategy );

        int[] unixTimeUTC = (int[]) data.get("UnixTimeUTC");
        Instant instant = Instant.ofEpochSecond(unixTimeUTC[0], unixTimeUTC[1] * 1000);
        DateTime dateTime = new DateTime(Date.from(instant)).withZoneRetainFields(DateTimeZone.UTC);

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

        double[] calibrated = applyDrsCalibration(data, patchTemperatures);
        data.put(this.outputKey, calibrated); //TODO check move into applyDrsCalibration

        return data;
    }

    /******************************************************************************************************************/
    private double[] applyDrsCalibration(Data data,  float[] patchTemperatures){
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
                vraw -= (this.baselineSlope[totalPos] * patchTemperatures[pixel/9] + this.baselineOffset[totalPos]);
                vraw /= (this.gainSlope[totalPos] * patchTemperatures[pixel/9] + this.gainOffset[totalPos]);
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