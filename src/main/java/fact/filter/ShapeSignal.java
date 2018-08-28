package fact.filter;

import fact.Constants;
import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by jbuss on 07.10.14.
 */
public class ShapeSignal implements Processor {

    static Logger log = LoggerFactory.getLogger(ShapeSignal.class);


    @Parameter(required = true)
    private String key;

    @Parameter(required = true)
    private String outputKey;

    @Parameter(required = true)
    int shift = 20;

    @Parameter(required = false)
    int skipLeft = 0;

    @Parameter(required = false)
    int skipRight = 0;

    @Parameter
    double factor = 0.66;


    @Override
    public Data process(Data input) {

        Utils.isKeyValid(input, key, double[].class);
        double[] data = (double[]) input.get(key);
//        double[] shifted_data = new double[data.length];
        double[] result = new double[data.length];

        int n_pixels = Constants.NUMBEROFPIXEL;
        int roi = data.length/1440;

        for (int pix = 0; pix < n_pixels; pix++) {
            double[] pixel_data = Arrays.copyOfRange(data, pix*roi+skipLeft, roi*(pix+1)-skipRight);
            double[] shifted_data = new double[pixel_data.length];

            for (int i=0 ; i < pixel_data.length ; i++)
            {
                shifted_data[(i+shift) % roi] = (-1) * factor * pixel_data[ i ];
            }

            for (int i=0 ; i < pixel_data.length ; i++)
            {
                int sl = Utils.absPos(pix,i+skipLeft,roi);
                result[sl] = data[sl] + shifted_data[i];
            }
        }

        input.put(outputKey, result);

        return input;
    }

    public void setShift(int shift) {
        this.shift = shift;
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

    public double getShift() {
        return shift;
    }

    public double getFactor() {
        return factor;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }
}
