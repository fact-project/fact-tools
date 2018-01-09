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
 * This class implements a simple Fir-Filter. See
 * http://en.wikipedia.org/wiki/Fir_filter for Details. The coefficients of the
 * are stored in an array {n, n-1, n-2, ..}. Values outside of the data domain
 * are treated as zeroes.
 *
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
@Description(group = "Fact Tools.Filter", text = "")
public class FirFilter implements Processor {
    static Logger log = LoggerFactory.getLogger(FirFilter.class);

    @Parameter(required = false, description = "Filter coefficents array. {n, n-1, n-2, ..}.", defaultValue = "{0.5f,0.2f, 0.1f}")
    public Double[] coefficients = {0.5, 0.2, 0.1};

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
        double[] result = new double[data.length];
        npix = (Integer) input.get("NPIX");

        // foreach pixel
        int roi = data.length / npix;
        for (int pix = 0; pix < npix; pix++) {
            // result[pix*roi] =
            // iterate over all slices
            for (int slice = 0; slice < roi; slice++) {
                int pos = pix * roi + slice;

                for (int i = Math.min(slice, coefficients.length - 1); i >= 0; i--) {
                    // System.out.println("i: " + i);
                    result[pos] += coefficients[i] * data[pos - i];
                }
            }
        }
        input.put(outputKey, result);
        return input;

    }
}
