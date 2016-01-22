/**
 * 
 */
package fact.extraction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.util.Interval;

/**
 * This processor simply calculates the maximum value for all time slices in
 * each Pixel. The output is a double array with an entry for each Pixel. The
 * <code>window</code> parameter is used to limit the search window for the
 * maximal amplitudes for each pixel.
 * 
 * By default, the search window ranges from slice 35 to 125.
 * 
 * The processor also computes the positions for the maximum values for each of
 * the pixels. The positions are written to an int[] attribute with key
 * <code>${outputKey}:pos</code>.
 * 
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;, Christian Bockermann
 * 
 */
public class MaxAmplitude implements Processor {
    static Logger log = LoggerFactory.getLogger(MaxAmplitude.class);

    @Parameter(required = true)
    private String key;

    @Parameter(required = true)
    private String outputKey;

    @Parameter(description = "The search window (slices), to determine max amplitudes for each pixel, default is '35,125'.")
    Interval window = new Interval(35, 125);

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, key, double[].class);
        Utils.isKeyValid(input, "NPIX", Integer.class);
        double[] data = (double[]) input.get(key);
        int npix = (Integer) input.get("NPIX");
        int roi = data.length / npix;

        // for all pixel find the maximum value
        double[] max = new double[npix];
        int[] maxPos = new int[npix];

        int from = window.start.intValue();
        int to = window.end.intValue();

        for (int pix = 0; pix < npix; pix++) {
            maximum(roi, pix, data, from, to, max, maxPos);
        }

        input.put(outputKey, max);
        input.put(outputKey + ":pos", maxPos);
        return input;
    }

    /**
     * Find the maximum value in the array. Searches in the window given by
     * <code>start</code> and <code>end</code>. The maximal values and their
     * positions are written to the given output array <code>maxValues</code>
     * and <code>maxPos</code>.
     * 
     * @param roi
     * @param pix
     *            pixel to be checked
     * @param data
     *            the array to be checked
     */
    public void maximum(int roi, int pix, double[] data, int start, int end, double[] maxValues, int[] maxPos) {
        double tempMaxValue = 0;
        for (int slice = start; slice < end; slice++) {
            int pos = pix * roi + slice;
            double value = data[pos];
            if (value > tempMaxValue) {
                tempMaxValue = value;
                maxValues[pix] = value;
                maxPos[pix] = slice;
            }
        }
    }
}