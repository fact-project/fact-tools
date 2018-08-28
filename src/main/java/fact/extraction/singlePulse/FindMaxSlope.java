package fact.extraction.singlePulse;

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
    public String key = null;

    @Parameter(required = true, description = "key of arrival time array")
    public String arrivalTimeKey = null;

    @Parameter(required = true, description = "key of arrival time array")
    public String derivationKey = null;

    @Parameter(required = true, description = "key of output array")
    public String outputKeyVisualization = null;

    @Parameter(required = true, description = "key of output array")
    public String outputKey = null;

    @Parameter(description = "size of the window to search for the max amplitude", defaultValue = "20")
    public int maxSearchSlice = 20;

    @Parameter(description = "number of slices to fit the slope", defaultValue = "1")
    public int numFitSlices = 8;

    @Override
    public Data process(Data item) {
        Utils.mapContainsKeys(item, key, arrivalTimeKey, derivationKey);

        double[] data = (double[]) item.get(key);
        double[] slopesArray = (double[]) item.get(derivationKey);
        int[][] arrivalTimes = (int[][]) item.get(arrivalTimeKey);

        double[] result = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            result[i] = 0;
        }

        double[][] pulseSlopes = new double[Constants.N_PIXELS][];

        int roi = data.length / Constants.N_PIXELS;

        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {

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

        item.put(outputKeyVisualization, result);
        item.put(outputKey, pulseSlopes);

        return item;
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
}
