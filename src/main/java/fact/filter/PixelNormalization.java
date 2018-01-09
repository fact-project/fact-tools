/**
 *
 */
package fact.filter;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Description;
import stream.annotations.Parameter;

/**
 * Normalizes all values in a pixel. That means only  0 &lt; value &lt; 1 are allowed in the output. This is done per pixel.
 * So the normalization is different in each one.
 *
 * @author Kai
 */
@Description(group = "Data Stream.FACT")
public class PixelNormalization implements Processor {

    static Logger log = LoggerFactory.getLogger(PixelNormalization.class);


    @Parameter(required = true)
    public String key;

    @Parameter(required = true)
    public String outputKey;

    @Override
    public Data process(Data input) {

        Utils.isKeyValid(input, key, double[].class);

        double[] data = (double[]) input.get(key);
        double[] normalizedSlices = new double[data.length];


        int pixels = 1440;
        int roi = data.length / pixels;

        for (int pix = 0; pix < pixels; pix++) {
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            for (int slice = 0; slice < roi; slice++) {
                int pos = pix * roi + slice;
                min = Math.min(min, data[pos]);
                max = Math.max(max, data[pos]);
            }

            double range = Math.abs(max - min);

            for (int slice = 0; slice < roi; slice++) {
                int pos = pix * roi + slice;
                normalizedSlices[pos] = data[pos] / range + Math.abs(min) / range;
            }
        }
        input.put(outputKey, normalizedSlices);
        return input;
    }
}
