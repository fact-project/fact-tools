package fact.datacorrection;

import fact.Utils;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.io.SourceURL;

/**
 * Created by maxnoe on 15.08.16.
 *
 * Calibrate the data using the timelapse method.
 * Application on MAGIC data is described here: https://arxiv.org/pdf/1305.1007.pdf
 *
 * The code to generate the fits files with the calibration constants can be found here:
 * https://github.com/fact-project/timelapse_calibration
 *
 * It seems that the drs4 cells have an offset which depends on the time since they were last read out of
 * the following form: amplitude = a * deltaT^b
 *
 * This Processor also converts adc counts to mV after the calibration.
 *
 * The deltaTs need to be calculated using the fact.utils.DrsCellLastReadout processor
 */

public class DrsTimelapseCalibration implements StatefulProcessor {

    private Logger log = LoggerFactory.getLogger(DrsTimelapseCalibration.class);

    private double adcCountsToMilliVolt = 2000.0 / 4096.0;

    @Parameter(description = "Data to calibrate", defaultValue = "Data")
    private String dataKey = "Data";

    @Parameter(description = "OutputKey", defaultValue = "DataCalibrated")
    private String outputKey = "DataCalibrated";

    @Parameter(required = true, description = "Fits file with the calibration constants",
               defaultValue = "Null. Will try to find path to fitParameterFile from the stream.")
    private SourceURL fitParameterFile = null;

    @Parameter(description = "Key to time since last readout of drscells", defaultValue = "deltaT")
    private String deltaTKey = "deltaT";

    @Parameter(description = "Key to the startcell data", defaultValue = "StartCellData") // why, is not a constant?
    private String startCellKey = "StartCellData";

    // The following keys are required to exist in the fit-parameter data
    private final static String[] fitParameterKeys = new String[]{"Multiplier", "Exponent"};

    private float[][] calibrationConstants;

    // The following keys are required to exist in the raw data
    private final static String[] dataKeys = new String[]{"NPIX", "NCELLS", "NROI"};

    @Override
    public Data process(Data data) {
        log.debug("----- process -----");

        Utils.mapContainsKeys(data, dataKeys);
        Utils.mapContainsKeys(data, dataKey, deltaTKey); // startCellKey/StartCellData ?

        int NRCHIDS = (int) data.get("NPIX");
        int NRCELLS = (int) data.get("NCELLS");
        int ROI = (int) data.get("NROI");
        double[] rawData = (double[]) data.get(dataKey);
        short[] startcells = (short[]) data.get(startCellKey);
        float[] deltaT = (float[]) data.get(deltaTKey);

        double[] dataCalibrated = new double[rawData.length];
        int startCell, sample_idx, cell_idx;
        for (int chid = 0; chid < NRCHIDS; chid++){
            for (int sample = 0; sample < ROI ; sample++) {

                startCell = Utils.sampleToCell(sample, startcells[chid], NRCELLS);
                sample_idx = chid * ROI + sample;
                cell_idx = chid * NRCELLS + startCell;

                double cellOffset = calculateOffset(deltaT[sample_idx], calibrationConstants[cell_idx]);


                dataCalibrated[sample_idx] = (rawData[sample_idx] - cellOffset) * adcCountsToMilliVolt;
            }
        }
        data.put(outputKey, dataCalibrated);
        return data;
    }

    private static double calculateOffset(float deltaT, float[] constants){

        if(Float.isNaN(deltaT)) {
            return 0.0;
        }
        return (double) constants[0] * Math.pow(deltaT, constants[1]);
    }

    @Override
    public void init(ProcessContext processContext) throws Exception {
        int NRCHIDS_ = 1440;
        int NRCELLS_ = 1024;
        int nrOfConstants = 2;
        calibrationConstants = new float[NRCHIDS_ * NRCELLS_][nrOfConstants];

        log.info("Reading timelapse calibration constants from {}", fitParameterFile.getPath());
        Fits fits = new Fits(fitParameterFile.getFile());
        BinaryTableHDU table = (BinaryTableHDU) fits.getHDU(1);

        for (int pixel=0; pixel < NRCHIDS_; pixel++){
            for(int cell=0; cell < NRCELLS_; cell++){
                int idx = pixel * NRCELLS_ + cell;
                Object[] row =  table.getRow(pixel * NRCELLS_ + cell);

                for (int i = 0; i < nrOfConstants; i++) {
                    calibrationConstants[idx][i] = ((float[]) row[i])[0];
                }
            }
        }
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

    public void setDeltaTKey(String deltaTKey) {
        this.deltaTKey = deltaTKey;
    }

    public void setStartCellKey(String startCellKey) {
        this.startCellKey = startCellKey;
    }
}
