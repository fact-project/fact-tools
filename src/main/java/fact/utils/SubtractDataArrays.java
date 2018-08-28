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
    public Data process(Data item) {
        Utils.isKeyValid(item, key, double[].class);
        Utils.isKeyValid(item, subtractedKey, double[].class);
        double[] subtractedArray = new double[Constants.N_PIXELS];

        double[] array1 = (double[]) item.get(key);
        double[] array2 = (double[]) item.get(subtractedKey);

        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
            subtractedArray[pix] = array1[pix] - array2[pix];
        }

        item.put(outputKey, subtractedArray);
        return item;
    }
}
