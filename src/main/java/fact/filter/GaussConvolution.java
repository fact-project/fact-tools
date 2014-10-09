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
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
@Description(group = "Fact Tools.Filter", text = "")
public class GaussConvolution implements Processor {
    static Logger log = LoggerFactory.getLogger(GaussConvolution.class);

    @Parameter(required = true)
    private String key;

    @Parameter(required = false, defaultValue = "1")
    private double variance = 1;

    @Parameter(required = true)
    private String outputKey;

    /**
     * Returns value of the gaussian function at point x with mean = 0 and variance = variance;
     * @param variance the width of your gaussian
     * @param x the point to evaluate
     * @return the value at x
     */
    private double gaussKernel(double variance, double x){
        variance *= 2;
        double r = (1/Math.sqrt(Math.PI*variance)) * Math.exp(-(Math.pow(x, 2)/(variance)));
        return r;
    }

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, key, double[].class);

        //see the wikipedia article. apparently this choice is typical
        int numSamples = (int) (4*Math.sqrt(variance) + 1);

        double[] data = (double[]) input.get(key);
        int npix = (Integer) input.get("NPIX");
        double[] result = new double[data.length];
        for (int i = 0; i < result.length; i++) {
            for (int m = -numSamples; m < numSamples; m++) {
                double dataValue;
                if( i + m < 0){
                    dataValue = data[i];
                } else if (i + m >= data.length){
                    dataValue = data[data.length - 1];
                } else {
                    dataValue = data[i + m];
                }
                result[i] += dataValue * gaussKernel(variance, m);
            }
        }
        input.put(outputKey, result);
        return input;

    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }


    public void setVariance(double variance) {
        this.variance = variance;
    }


}