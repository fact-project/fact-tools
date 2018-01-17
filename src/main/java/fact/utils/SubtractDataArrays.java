/**
 *
 */
package fact.utils;

import fact.Constants;
import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Subtract two Pixel Arrays of the same size from each other
 *
 * @author jbuss
 */
public class SubtractDataArrays implements Processor {
    static Logger log = LoggerFactory.getLogger(RemappingKeys.class);

    @Parameter(required = true, description = "The key to your data array.")
    public String key;

    @Parameter(required = true, description = "The key to your subtracted data array.")
    public String subtractedKey;

    @Parameter(required = false)
    public String outputKey;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, key, double[].class);
        Utils.isKeyValid(input, subtractedKey, double[].class);
        double[] subtractedArray = new double[Constants.NUMBEROFPIXEL];

        double[] array1 = (double[]) input.get(key);
        double[] array2 = (double[]) input.get(subtractedKey);

        for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
            subtractedArray[pix] = array1[pix] - array2[pix];
        }

        input.put(outputKey, subtractedArray);
        return input;
    }
}
