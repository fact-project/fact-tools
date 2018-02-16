package fact.extraction.singlePulse;

import fact.Constants;
import fact.Utils;
import fact.filter.MovingLinearFit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;

/**
 * Calculate the FWHM of a pulse in a list of pulses.
 * Created by jbuss on 14.10.14.
 */
public class FWHMPulses implements Processor {
    static Logger log = LoggerFactory.getLogger(MovingLinearFit.class);

    @Parameter(required = true, description = "key of data array")
    public String key = null;

    @Parameter(required = true, description = "key of max position array")
    public String maxPosKey = null;

    @Parameter(required = true, description = "key of min position array")
    public String minPosKey = null;

    @Parameter(required = true, description = "key of output array")
    public String outputKey = null;

    @Parameter(description = "key of output for visualisation")
    public String visualizationKey = null;

    @Override
    public Data process(Data item) {
        /**
         * Takes the data array,
         * Takes the position of the maximum
         * Takes the global minimum slices of each pixel
         * global minimum as offset
         * Shift amplitudes to be minimum slice at 0
         * calculate FWHM for pulse
         */

        Utils.mapContainsKeys(item, key, maxPosKey, minPosKey);

        double[] data = (double[]) item.get(key);
        double[] minAmplitudes = (double[]) item.get(minPosKey);
        int[][] maxPosArrayList = (int[][]) item.get(maxPosKey);

        double[][] widthArrayList = new double[Constants.N_PIXELS][];
        double[] visualisation = new double[data.length];

        int roi = data.length / Constants.N_PIXELS;

        for (int pix = 0; pix < maxPosArrayList.length; pix++) {
            double offset = minAmplitudes[pix];

            ArrayList<Double> widthList = new ArrayList<>();

            for (int pulse = 0; pulse < maxPosArrayList[pix].length; pulse++) {
                int maxPos = pix * roi + maxPosArrayList[pix][pulse];
                double maxAmplitude = data[maxPos] - offset;

                visualisation[maxPos] = data[maxPos];

                int right = 0;
                int left = 0;
                double rightAmplitude = maxAmplitude;
                double leftAmplitude = maxAmplitude;

                boolean out_of_bounds = false;

                while (rightAmplitude >= maxAmplitude / 2 || leftAmplitude >= maxAmplitude / 2) {

                    if (rightAmplitude >= maxAmplitude / 2) {
                        right++;
                        if (maxPos + right >= (pix + 1) * roi) {
//                            log.error(String.format("(right) slices out of bounds in pixel %s at %s", pix, maxPos+right));
                            out_of_bounds = true;
                            break;
                        }

                        rightAmplitude = data[maxPos + right] - offset;
                        visualisation[maxPos + right] = data[maxPos] / 2;
                    }

                    if (leftAmplitude >= maxAmplitude / 2) {
                        left++;
                        if (maxPos - left < pix * roi) {
//                            log.error(String.format("(left) slices out of bounds in pixel %s at %s", pix, maxPos-left));
                            out_of_bounds = true;
                            break;
                        }
                        leftAmplitude = data[maxPos - left] - offset;
                        visualisation[maxPos - left] = data[maxPos] / 2;
                    }
                }
                if (out_of_bounds == false) {
                    widthList.add((double) right + left);
                } else {
                    widthList.add(Double.NaN);
                }
            }
            widthArrayList[pix] = new double[widthList.size()];
            widthArrayList[pix] = Utils.arrayListToDouble(widthList);
        }
        item.put(outputKey, widthArrayList);
        item.put(visualizationKey, visualisation);


        return item;
    }
}
