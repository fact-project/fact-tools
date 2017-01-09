package fact.datacorrection;

import com.google.common.collect.ImmutableMap;
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
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.time.format.DateTimeFormatter;
import java.time.*;
import java.util.Date;



import org.joda.time.DateTime; // TODO remove when no longer needed
import org.joda.time.DateTimeZone; // TODO remove when no longer needed

/**
 * Created by florian on 06.12.16.
 */
public class DrsTemperatureCalibration implements StatefulProcessor {

    private Logger log = LoggerFactory.getLogger(DrsTemperatureCalibration.class);

    /******************************************************************************************************************/
    @Override
    public void init(ProcessContext processContext_) throws Exception {
        log.debug("----- init -----");

        if (this.url_fitValueFile != null) {
            try {
                loadDrsFitParameter(this.url_fitValueFile);
            } catch (Exception e) {
                log.error("Could not load file specified in the url_fitValueFile.");
                throw new RuntimeException(e.getMessage());
            }
        } else {
            log.error("this.url_fitValueFile == null");
        }

        this.pixelCount = 1440;//(int) data_.get("NPIX"); //TODO get from file
        this.cellCount = 1024;//(int) data_.get("NCELLS"); //TODO get from file
        this.ROI = 300;//(int) data_.get("NCELLS"); //TODO get from file
    }

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
    @Parameter(required = false, description = "A URL to the DRS-FitValue data (in FITS formats)",
            defaultValue = "Null. Will try to find path to DRS-FitValueFile from the stream.")
    private SourceURL url_fitValueFile = null;

    public void setUrl_fitValueFile(SourceURL url_fitValueFile) {
        this.url_fitValueFile = url_fitValueFile;
    }

    private File currentFitValueFile = new File("");
    Data fitValueData = null;

    /******************************************************************************************************************/
    @Parameter(required = true, description = "Name of the service that provides aux files")
    private AuxiliaryService auxService;

    public void setAuxService(AuxiliaryService auxService) {
        this.auxService = auxService;
    }

    private AuxPointStrategy auxPointStrategy = new Closest();
    /******************************************************************************************************************/

    private int pixelCount = 0;
    private int cellCount = 0;
    private int ROI = 0;

    /******************************************************************************************************************/
    //Just for temporary storage
    private float[] baselineGradient = null;
    private float[] baselineOffset = null;
    private float[] gainGradient = null;
    private float[] gainOffset = null;

    /******************************************************************************************************************/
    // The following keys are required to exist in the DRS-FitValue data
    private final static String[] fitValueKeys = new String[]{"gradient", "offset"};

    /******************************************************************************************************************/
    // The following keys are required to exist in the raw data
    private final static String[] dataKeys = new String[]{"DATE-OBS", "DATE-END"};//, "NPIX", "NCELLS"}; //element of the header

    /******************************************************************************************************************/
    private  void loadDrsFitParameter(SourceURL url_fitValueFile_)
    {
        log.debug("----- loadDrsFitParameter -----");
        try {
            ZFitsStream stream = new ZFitsStream(url_fitValueFile_);

            stream.tableName = "BASELINE";
            stream.init();
            fitValueData = stream.readNext();

            log.debug("Read DRS-fitValueData data: {}", fitValueData);

            // this for-loop is simply a check that fires an exception if any
            // of the expected keys is missing in table 'BASELINE'
            //
            for (String key : fitValueKeys) {
                if (!fitValueData.containsKey(key)) {
                    throw new RuntimeException("DRS-FitValue data('BASELINE') is missing key '"
                            + key + "'!");
                }
            }

            this.baselineGradient = ((float[]) fitValueData.get("gradient"));
            this.baselineOffset = ((float[]) fitValueData.get("offset"));

            log.debug("baselineGradient data: {}", baselineGradient);
            log.debug("baselineOffset data: {}", baselineOffset);

            stream.tableName = "GAIN";
            stream.init();
            fitValueData = stream.readNext();

            // this for-loop is simply a check that fires an exception if any
            // of the expected keys is missing in table 'GAIN'
            //
            for (String key : fitValueKeys) {
                if (!fitValueData.containsKey(key)) {
                    throw new RuntimeException("DRS-FitValue data('GAIN') is missing key '"
                            + key + "'!");
                }
            }
            this.gainGradient = ((float[]) fitValueData.get("gradient"));
            this.gainOffset = ((float[]) fitValueData.get("offset"));

            log.debug("gainGradient data: {}", gainGradient);
            log.debug("gainOffset data: {}", gainOffset);

        } catch (Exception e) {

            log.error("Failed to load DRS data: {}", e.getMessage());
            if (log.isDebugEnabled())
                e.printStackTrace();

            this.fitValueData = null; //TODO check is neseccary to set to 0

            throw new RuntimeException(e.getMessage());
        }
    }

    /******************************************************************************************************************/
    @Override
    public Data process(Data data) {
        log.debug("----- process -----");

        if (this.url_fitValueFile == null) { //TODO check needed
            //file not loaded yet. try to find by magic.
            File fitValueFile = (File) data.get("@fitValueFile");
            if (fitValueFile != null) {
                if (!fitValueFile.equals(this.currentFitValueFile)) {
                    this.currentFitValueFile = fitValueFile;
                    try {
                        log.debug("Using File " + fitValueFile.getAbsolutePath());
                        loadDrsFitParameter(new SourceURL(fitValueFile.toURI().toURL()));
                    } catch (MalformedURLException e) {
                        //pass.
                    }
                }
            } else {
                throw new IllegalArgumentException("No DRS-FitValue File set or no @fitValueFile key in data stream");
            }
        }

        for (String key : dataKeys) {
            if (!data.containsKey(key)) {
                throw new RuntimeException("data is missing key '" + key + "'!");
            }
        }

        log.debug("Get patchTemperaure from AuxiliaryService with AuxPointStrategy {}", this.auxPointStrategy );

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;//ofPattern("YYYY-MM-dd'T'HH:mm:ss.SSSnnn") dosent work

        Instant timestamp_beg = LocalDateTime.parse((String) data.get("DATE-OBS"), formatter).toInstant(ZoneOffset.UTC);
        Instant timestamp_end = LocalDateTime.parse((String) data.get("DATE-END"), formatter).toInstant(ZoneOffset.UTC);

        long timeDifferenceInNanosec = (long)(((long)timestamp_beg.getNano() + (long)timestamp_end.getNano())/2 +
                                              (timestamp_beg.getEpochSecond() + timestamp_end.getEpochSecond())/2 * Math.pow(10,9));

        Instant timestamp_middle = Instant.ofEpochSecond((timeDifferenceInNanosec) / (long) Math.pow(10,9),
                                                         (timeDifferenceInNanosec) % (long) Math.pow(10,9));

        DateTime dateTime_middle = new DateTime(Date.from(timestamp_middle)).withZoneRetainFields(DateTimeZone.UTC); // TODO remove when no longer needed
        dateTime_middle = dateTime_middle.minusHours(2); // TODO remove when no longer needed

        AuxPoint auxPoint;
        try {
            auxPoint = auxService.getAuxiliaryData(AuxiliaryServiceName.FAD_CONTROL_TEMPERATURE, dateTime_middle, this.auxPointStrategy);
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


        log.debug("Processing Data item by applying DRS calibration...");

        short[] startCellVector = (short[]) data.get("StartCellData");
        if (startCellVector == null) {
            log.error(" data .fits file did not contain startcell data. cannot apply drscalibration");
            return null;
        }

        short[] rawData = (short[]) data.get(this.dataKey);
        if (rawData == null) {
            log.error(" data .fits file did not contain the value for the key "
                    + this.dataKey + ". cannot apply drsCalibration");
            throw new RuntimeException(
                    " data .fits file did not contain the value for the key \"" + this.dataKey + "\". Cannot apply drs calibration)");
        }

        double[] rawfloatData = new double[rawData.length];
        // System.arraycopy(rawData, 0, rawfloatData, 0, rawfloatData.length);
        for (int i = 0; i < rawData.length; i++) {
            rawfloatData[i] = rawData[i];
        }

        double[] calibrated = applyDrsCalibration(rawfloatData, startCellVector, patchTemperatures);
        data.put(this.outputKey, calibrated);


        return data;
    }

    /******************************************************************************************************************/
    private double[] applyDrsCalibration(double[] rawData, short[] startCellVector, float[] patchTemperatures){
        log.debug("----- applyDrsCalibration -----");

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
        for (int pixel = 0; pixel < this.pixelCount; pixel++) {
            for (int slice = 0; slice < this.ROI; slice++) {

                startCell = 0;
                if (startCellVector[pixel] != -1) {
                    startCell = startCellVector[pixel];
                }
                pos = pixel * this.ROI + slice;
                totalPos = pixel * this.cellCount + ((startCell + slice) % (this.cellCount));

                vraw = rawData[pos] * dconv;
                vraw -= (this.baselineGradient[totalPos] * patchTemperatures[pixel/9] + this.baselineOffset[totalPos]);
                vraw /= (this.gainGradient[totalPos] * patchTemperatures[pixel/9] + this.gainOffset[totalPos]);
                vraw *= 1907.35;

                calibrated[pos] = vraw;

            }
        }
        log.debug("destination.length {} elements", calibrated.length);
        return calibrated;
    }

    /******************************************************************************************************************/
    @Override
    public void resetState() throws Exception { }

    /******************************************************************************************************************/
    @Override
    public void finish() throws Exception { }

}