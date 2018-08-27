/**
 *
 */
package fact.filter;

import fact.Utils;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

/**
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class GaussConvolution implements StatefulProcessor {

    @Parameter(required = true)
    public String key;

    @Parameter(required = false, defaultValue = "1")
    public double variance = 1;

    @Parameter(required = true)
    public String outputKey;

    private int numSamples;
    private double[] coefficents;


    /**
     * Returns value of the gaussian function at point x with mean = 0 and variance = variance;
     *
     * @param variance the width of your gaussian
     * @param x        the point to evaluate
     * @return the value at x
     */
    private double gaussKernel(double variance, double x) {
        variance *= 2;
        double r = (1 / Math.sqrt(Math.PI * variance)) * Math.exp(-(Math.pow(x, 2) / (variance)));
        return r;
    }


    @Override
    public void init(ProcessContext context) throws Exception {
        //see the wikipedia article. apparently this choice is typical
        numSamples = (int) (4 * Math.sqrt(variance) + 1);
        coefficents = new double[2 * numSamples + 1];
        for (int m = -numSamples; m < numSamples; m++) {
            coefficents[m + numSamples] = gaussKernel(variance, m);
        }
    }

    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, key, double[].class);
//        Stopwatch stopwatch = Stopwatch.createUnstarted();


        double[] data = (double[]) item.get(key);

        double[] result = new double[data.length];
        for (int i = numSamples; i < result.length - numSamples - 1; i++) {
            for (int m = -numSamples; m < numSamples; m++) {
                result[i] += data[i + m] * coefficents[m + numSamples];
            }
        }
        item.put(outputKey, result);
        return item;

    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }
}
