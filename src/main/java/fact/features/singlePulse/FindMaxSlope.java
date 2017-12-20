package fact.feaphotonstreamtures.singlePulse;

import fact.Constants;
import fact.Utils;
import fact.filter.MovingLinearFit;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;

/**
 * Created by jbuss on 09.10.14.
 */
public class FindMaxSlope implements Processor {
    static Logger log = LoggerFactory.getLogger(MovingLinearFit.class);

    @Parameter(required = true, description = "key of data array")
    String key = null;

    @Parameter(required = true, description = "key of arrival time array")
    String arrivalTimeKey = null;

    @Parameter(required = true, description = "key of arrival time array")
    String derivationKey = null;

    @Parameter(required = true, description = "key of output array")
    String outputKeyVisualization = null;

    @Parameter(required = true, description = "key of output array")
    String outputKey = null;

    @Parameter(description = "size of the window to search for the max amplitude", defaultValue = "20")
    int maxSearchSlice = 20;

    @Parameter(description = "number of slices to fit the slope", defaultValue = "1")
    int numFitSlices = 8;

    @Override
    public Data process(Data input) {
        Utils.mapContainsKeys(input, key, arrivalTimeKey, derivationKey);

        double[] data = (double[]) input.get(key);
        double[] slopesArray = (double[]) input.get(derivationKey);
        int[][] arrivalTimes = (int[][]) input.get(arrivalTimeKey);

        double[] result = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            result[i] = 0;
        }

        double[][] pulseSlopes = new double[Constants.NUMBEROFPIXEL][];

        int roi = data.length / Constants.NUMBEROFPIXEL;

        for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {

            int numberPulses = arrivalTimes[pix].length;

            ArrayList<Double> slopes = new ArrayList<Double>();

            for (int i = 0; i < numberPulses; i++) {
                int arrTime = (int) arrivalTimes[pix][i];
                int pos = (pix * roi) + arrTime;

//                //calculate Position of the local maximum
//                int maxAmplitudePos = findMaxAmplitude(arrTime, maxSearchSlice, pix, roi, data);
//                if (maxAmplitudePos < arrTime){
//                    continue;
//                }

                //find max Slope after arrival time
                int maxSlopePos = findMaxSlopePos(pos, maxSearchSlice, slopesArray);

                //calculate slope
                double slope = calculateSlope(maxSlopePos, numFitSlices, data);

                slopes.add(slope);

                result[maxSlopePos] = slope;

            }
            pulseSlopes[pix] = new double[slopes.size()];
            pulseSlopes[pix] = Utils.arrayListToDouble(slopes);

        }

        input.put(outputKeyVisualization, result);
        input.put(outputKey, pulseSlopes);

        return input;
    }

    public int findMaxSlopePos(int arrTime, int maxSearchSlice, double[] slopesArray) {
        double maxSlope = slopesArray[arrTime];
        int maxSlopePos = arrTime;
        for (int slice = arrTime; slice < arrTime + maxSearchSlice; slice++) {
            if (slice >= slopesArray.length) {
                break;
            }
            if (slopesArray[slice] < 0) {
                continue;
            }
            if (maxSlope > slopesArray[slice] && maxSlope > slopesArray[slice + 5]) {
                break;
            }
            maxSlope = slopesArray[slice];
            maxSlopePos = slice;
        }
        return maxSlopePos;

    }

    public double calculateSlope(int slopePos, int numSlices, double[] data) {
        //calculate slope
        SimpleRegression regression = new SimpleRegression();

        for (int j = 0; j < numSlices; j++) {
            regression.addData(j, data[(j + slopePos - numSlices / 2) % data.length]);
        }
        regression.regress();

        return regression.getSlope();

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getArrivalTimeKey() {
        return arrivalTimeKey;
    }

    public void setArrivalTimeKey(String arrivalTimeKey) {
        this.arrivalTimeKey = arrivalTimeKey;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public int getMaxSearchSlice() {
        return maxSearchSlice;
    }

    public void setMaxSearchSlice(int maxSearchSlice) {
        this.maxSearchSlice = maxSearchSlice;
    }

    public int getNumFitSlices() {
        return numFitSlices;
    }

    public void setNumFitSlices(int numFitSlices) {
        this.numFitSlices = numFitSlices;
    }

    public String getDerivationKey() {
        return derivationKey;
    }

    public void setDerivationKey(String derivationKey) {
        this.derivationKey = derivationKey;
    }

    public String getOutputKeyVisualization() {
        return outputKeyVisualization;
    }

    public void setOutputKeyVisualization(String outputKeyVisualization) {
        this.outputKeyVisualization = outputKeyVisualization;
    }
}
