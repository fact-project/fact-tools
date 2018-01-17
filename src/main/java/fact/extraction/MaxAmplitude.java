/**
 *
 */
package fact.extraction;

import fact.Constants;
import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This processor simply calculates the maximum value for all time slices in each Pixel.
 * The output is a double array with an entry for each Pixel.
 * TODO: Refactor to only search inside a window
 *
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class MaxAmplitude implements Processor {
    static Logger log = LoggerFactory.getLogger(MaxAmplitude.class);

    @Parameter(required = true)
    public String key;

    @Parameter(required = true)
    public String outputKey;


    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, key, double[].class);
        double[] data = (double[]) item.get(key);
        int roi = data.length / Constants.N_PIXELS;

        //for all pixel find the maximum value
        double[] max = new double[Constants.N_PIXELS];

        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
            max[pix] = maximum(roi, pix, data);
        }

        item.put(outputKey, max);
        return item;
    }

    /**
     * Find the maximum value in the array. searchs in the window from pix * roi + slice to pix * roi + (slice + roi -1)
     *
     * @param roi
     * @param pix  pixel to be checked
     * @param data the array to be checked
     * @return
     */
    public double maximum(int roi, int pix, double[] data) {
        double tempMaxValue = 0;
        for (int slice = 0; slice < roi; slice++) {
            int pos = pix * roi + slice;
            double value = data[pos];
            if (value > tempMaxValue) {
                tempMaxValue = value;
            }
        }
        return tempMaxValue;
    }
}
