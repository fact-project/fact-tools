/**
 *
 */
package fact.extraction.singlePulse;

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
    public Data process(Data item) {
        Utils.mapContainsKeys(item, key, arrivalTimeKey);

        double[] data = (double[]) item.get(key);
        int roi = data.length / Constants.N_PIXELS;
        int[][] arrivalTimes = (int[][]) item.get(arrivalTimeKey);
        double[][] pulseSizes = new double[Constants.N_PIXELS][];

        //for each pixel
        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
            pulseSizes[pix] = new double[arrivalTimes[pix].length];
            pulseSizes[pix] = calculateSizes(pix, roi, data, arrivalTimes);
        }
        item.put(outputKey, pulseSizes);

        return item;
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
