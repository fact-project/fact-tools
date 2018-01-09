/**
 *
 */
package fact.extraction;

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

    private int npix;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, key, double[].class);
        Utils.isKeyValid(input, "NPIX", Integer.class);
        double[] data = (double[]) input.get(key);
        npix = (Integer) input.get("NPIX");
        int roi = data.length / npix;

        //for all pixel find the maximum value
        double[] max = new double[npix];

        for (int pix = 0; pix < npix; pix++) {
            max[pix] = maximum(roi, pix, data);
        }

        input.put(outputKey, max);
        return input;
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
