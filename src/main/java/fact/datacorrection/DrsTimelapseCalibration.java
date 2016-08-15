package fact.datacorrection;

import com.sun.org.apache.xpath.internal.SourceTree;
import fact.Constants;
import fact.Utils;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.util.ArrayFuncs;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.util.DoubleArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import java.util.Arrays;

/**
 * Created by maxnoe on 15.08.16.
 */
public class DrsTimelapseCalibration implements StatefulProcessor {

    private Logger log = LoggerFactory.getLogger(DrsTimelapseCalibration.class);

    @Parameter(required = true, description = "Fits file with the calibration constants")
    private SourceURL url = null;

    @Parameter(description = "Data to calibrate", defaultValue = "Data")
    private String dataKey = "Data";

    @Parameter(description = "Key to time since last readout of drscells", defaultValue = "deltaT")
    private String deltaTKey = "deltaT";

    @Parameter(description = "Key to the startcell data", defaultValue = "StartCellData")
    private String startCellKey = "StartCellData";

    @Parameter(description = "OutputKey", defaultValue = "DataCalibrated")
    private String outputKey = "DataCalibrated";

    private float[][] calibrationConstants;

    private int num_cells = 1024;

    private double adcCountsToMilliVolt = 2000.0 / 4096.0;

    @Override
    public Data process(Data item) {
        Utils.mapContainsKeys(item, deltaTKey, dataKey, "NROI");
        int roi = (int) item.get("NROI");
        short[] data = (short[]) item.get(dataKey);
        short[] startcells = (short[]) item.get(startCellKey);
        double[] deltaT = (double[]) item.get(deltaTKey);

        double[] dataCalibrated = new double[data.length];

        for (int pixel=0; pixel < Constants.NUMBEROFPIXEL; pixel++){
            for (int sample = 0; sample < roi ; sample++) {

                int cell = Utils.sampleToCell(sample, startcells[pixel], num_cells);
                int sample_idx = pixel * roi + sample;
                int cell_idx = pixel * num_cells + cell;

                float[] constants = calibrationConstants[cell_idx];


                dataCalibrated[sample_idx] = ((double) data[sample_idx] - offset(deltaT[sample_idx], constants)) * adcCountsToMilliVolt;
            }
        }
        item.put(outputKey, dataCalibrated);
        return item;
    }

    private static double test(double deltaT, float[] constants){
        if(Double.isNaN(deltaT)) {
            return constants[2];
        }
        return 0.0;
    }

    private static double offset(double deltaT, float[] constants){

        if(Double.isNaN(deltaT)) {
            return constants[2];
        }
        return constants[0] * Math.pow(deltaT, constants[1]) + constants[2];
    }

    @Override
    public void init(ProcessContext processContext) throws Exception {
        calibrationConstants = new float[Constants.NUMBEROFPIXEL * num_cells][3];

        log.info("Reading timelapse calibration constants from {}", url.getPath());
        Fits fits = new Fits(url.getFile());
        BinaryTableHDU table = (BinaryTableHDU) fits.getHDU(1);

        for (int pixel=0; pixel < Constants.NUMBEROFPIXEL; pixel++){
            for(int cell=0; cell < num_cells; cell++){
                int idx = pixel * num_cells + cell;
                Object[] row =  table.getRow(pixel * num_cells + cell);

                for (int i = 0; i < 3; i++) {
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

    public void setUrl(SourceURL url) {
        this.url = url;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public void setDeltaTKey(String deltaTKey) {
        this.deltaTKey = deltaTKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public void setStartCellKey(String startCellKey) {
        this.startCellKey = startCellKey;
    }
}
