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
 * This processor simply calculates the minimum value for all time slices in each Pixel.
 * The output is a double array with an entry for each Pixel.
 * TODO: REfactor to only search inside a window
 *@author Jens Buß &lt;jens.buss@tu-dortmund.de&gt;
 * 
 */
public class MinAmplitudeGlobal implements Processor{
	static Logger log = LoggerFactory.getLogger(MinAmplitudeGlobal.class);

    @Parameter(required = true)
    private String key;
    @Parameter(required = true)
    private String outputKey;

    @Parameter(description = "skip the first N slices of the timeline")
    private int skipFirstSlices=50;

    @Parameter(description = "skip the last N slices of the timeline")
    private int skipLastSlices=100;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, key, double[].class);
        double[] data = (double[]) input.get(key);
        int roi = data.length / Constants.NUMBEROFPIXEL;

        //for all pixel find the maximum value
        double[] min = new double[Constants.NUMBEROFPIXEL];

        for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
            min[pix] = globalMinimum(roi, pix, data);
        }

        input.put(outputKey, min);
        return input;
    }

    /**
     * Find the minimum value in the array. searches in the window from pix * roi + slice to pix * roi + (slice + roi -1)
     * @param roi
     * @param pix pixel to be checked
     * @param data the array to be checked
     * @return
     */
    public double globalMinimum(int roi, int pix, double[] data){
        double tempMinValue = Double.MAX_VALUE;
        for (int slice = skipFirstSlices; slice < roi-skipLastSlices; slice++) {
            int pos = pix * roi + slice;
            double value = data[pos];
            if (value < tempMinValue) {
                tempMinValue = value;
            }
        }
        return tempMinValue;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public int getSkipFirstSlices() {
        return skipFirstSlices;
    }

    public void setSkipFirstSlices(int skipFirstSlices) {
        this.skipFirstSlices = skipFirstSlices;
    }

    public int getSkipLastSlices() {
        return skipLastSlices;
    }

    public void setSkipLastSlices(int skipLastSlices) {
        this.skipLastSlices = skipLastSlices;
    }
}
