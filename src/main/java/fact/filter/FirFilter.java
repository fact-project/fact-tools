/**
 * 
 */
package fact.filter;

import fact.Constants;
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
 * 
 */
@Description(group = "Fact Tools.Filter", text = "")
public class FirFilter implements Processor {
    static Logger log = LoggerFactory.getLogger(FirFilter.class);

    @Parameter(required = false, description = "Filter coefficents array. {n, n-1, n-2, ..}.", defaultValue = "{0.5f,0.2f, 0.1f}")
    double[] coefficients = {0.5f, 0.2f, 0.1f};

    @Parameter(required = true)
    private String key;

    @Parameter(required = true)
    private String outputKey;


    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, key, double[].class);
        double[] data = (double[]) input.get(key);
        double[] result = new double[data.length];

        // foreach pixel
        int roi = data.length / Constants.NUMBEROFPIXEL;
        for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
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

    public void setCoefficients(double[] coefficients) {
        this.coefficients = coefficients;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

}