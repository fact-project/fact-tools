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
 * This processor simply calculates the minimum value for all time slices in each Pixel.
 * The output is a double array with an entry for each Pixel.
 * TODO: Refactor to only search inside a window
 *
 * @author Jens Buß &lt;jens.buss@tu-dortmund.de&gt;
 */
public class MinAmplitudeGlobal implements Processor {
    static Logger log = LoggerFactory.getLogger(MinAmplitudeGlobal.class);

    @Parameter(required = true)
    public String key;

    @Parameter(required = true)
    public String outputKey;

    @Parameter(description = "skip the first N slices of the timeline")
    public int skipFirstSlices = 50;

    @Parameter(description = "skip the last N slices of the timeline")
    public int skipLastSlices = 100;

    private int npix;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, key, double[].class);

        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");

        double[] data = (double[]) input.get(key);
        int roi = data.length / npix;

        //for all pixel find the maximum value
        double[] min = new double[npix];

        for (int pix = 0; pix < npix; pix++) {
            min[pix] = globalMinimum(roi, pix, data);
        }

        input.put(outputKey, min);
        return input;
    }

    /**
     * Find the minimum value in the array. searches in the window from pix * roi + slice to pix * roi + (slice + roi -1)
     *
     * @param roi
     * @param pix  pixel to be checked
     * @param data the array to be checked
     * @return
     */
    public double globalMinimum(int roi, int pix, double[] data) {
        double tempMinValue = Double.MAX_VALUE;
        for (int slice = skipFirstSlices; slice < roi - skipLastSlices; slice++) {
            int pos = pix * roi + slice;
            double value = data[pos];
            if (value < tempMinValue) {
                tempMinValue = value;
            }
        }
        return tempMinValue;
    }
}
