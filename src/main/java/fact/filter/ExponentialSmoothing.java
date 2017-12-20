package fact.filter;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;


/**
 * Calculates first Order exponential Smoothing
 * Let y be the original Series and s be the smoothed one.
 * s_0 = y_0
 * s_i = alpha*y_i + (1-alpha) * s_(i-1)
 * see http://en.wikipedia.org/wiki/Exponential_smoothing
 *
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class ExponentialSmoothing implements Processor {
    static Logger log = LoggerFactory.getLogger(ExponentialSmoothing.class);

    @Parameter(required = true, description = "This value changes the amount of smoothing that will take place. " +
            "If alpha equals 1 the values remain unchanged.  " +
            "See http://en.wikipedia.org/wiki/Exponential_smoothing", min = 0.0, max = 1.0, defaultValue = "0.5")
    double alpha = 0.5;

    @Parameter(required = true, description = "The key to the double array to smooth")
    String key;

    @Parameter(required = true, description = "The outputKey to which the smoothed data will be written to the stream")
    String outputKey;

    private int npix;

    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, key, double[].class);
        Utils.isKeyValid(item, "NPIX", Integer.class);
        double[] data = (double[]) item.get(key);
        npix = (Integer) item.get("NPIX");

        int roi = data.length / npix;
        double[] smoothedData = new double[data.length];
        //foreach pixel
        for (int pix = 0; pix < npix; pix++) {
            //beginn with startvalue
            smoothedData[pix * roi] = data[pix * roi];
            for (int slice = 1; slice < roi; slice++) {
                int pos = pix * roi + slice;
                //glaettung
                smoothedData[pos] = alpha * data[pos] + (1 - alpha) * smoothedData[pos - 1];
            }
        }
        item.put(outputKey, smoothedData);
        return item;
    }


    /*
     * Getter and Setter
     */
    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
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

}
