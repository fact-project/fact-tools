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
public class DividePixelArrays implements Processor {
    static Logger log = LoggerFactory.getLogger(RemappingKeys.class);

    @Parameter(required = true, description = "The key to your data array.")
    public String numeratorKey;

    @Parameter(required = true, description = "The key to your subtracted data array.")
    public String denominatorKey;

    @Parameter(required = false)
    public String outputKey;

    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, numeratorKey, double[].class);
        Utils.isKeyValid(item, denominatorKey, double[].class);

        double[] dividedArray = new double[Constants.N_PIXELS];

        double[] numerator = (double[]) item.get(numeratorKey);
        double[] denominator = (double[]) item.get(denominatorKey);

        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
            if (denominator[pix] != 0) {
                dividedArray[pix] = numerator[pix] / denominator[pix];
            } else {
                dividedArray[pix] = Double.NaN;
            }
        }

        item.put(outputKey, dividedArray);
        return item;
    }
}
