/**
 *
 */
package fact.features.singlePulse;

import fact.Constants;
import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;

/**
 * Finds sum of slice amplitudes starting at pulse arrival time
 *
 * @author Katie Gray &lt;kathryn.gray@tu-dortmund.de&gt;
 */
public class PulseSizeCalculator implements Processor {
    static Logger log = LoggerFactory.getLogger(PulseSizeCalculator.class);

    @Parameter(required = true)
    public String key;

    @Parameter(required = true)
    public String outputKey;
    //size of pulse

    @Parameter(required = true)
    public String arrivalTimeKey;
    //positions of arrival times

    @Parameter(required = true)
    public int width;
    //number of slices over which we integrate

    @Override
    public Data process(Data input) {
        Utils.mapContainsKeys(input, key, arrivalTimeKey);

        double[] data = (double[]) input.get(key);
        int roi = data.length / Constants.NUMBEROFPIXEL;
        int[][] arrivalTimes = (int[][]) input.get(arrivalTimeKey);
        double[][] pulseSizes = new double[Constants.NUMBEROFPIXEL][];

        //for each pixel
        for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
            pulseSizes[pix] = new double[arrivalTimes[pix].length];
            pulseSizes[pix] = calculateSizes(pix, roi, data, arrivalTimes);
        }
        input.put(outputKey, pulseSizes);

        return input;
    }

    /**
     * @param pix  Pixel to check
     * @param roi  Basically the number of slices in one event
     * @param data the array which to check
     * @return
     */

    public double[] calculateSizes(int pix, int roi, double[] data, int[][] arrivalTimes) {

        ArrayList<Double> sizes = new ArrayList<Double>();

        if (arrivalTimes[pix].length > 0) {
            int numberPulses = arrivalTimes[pix].length;
            for (int i = 0; i < numberPulses; i++) {
                double integral = 0;
                int start = arrivalTimes[pix][i];
                for (int slice = start; slice < start + width; slice++) {
                    int pos = pix * roi + slice;
                    integral += data[pos];
                }
                sizes.add(integral);
            }
        }
        return Utils.arrayListToDouble(sizes);
    }
}
