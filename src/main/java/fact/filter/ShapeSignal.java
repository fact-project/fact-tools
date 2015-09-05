package fact.filter;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

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
    int shift = 10;

    @Parameter
    double factor = 0.66;


    @Override
    public Data process(Data input) {

        Utils.isKeyValid(input, key, double[].class);
        double[] data = (double[]) input.get(key);
        double[] shifted_data = new double[data.length];
        double[] result = new double[data.length];


        for (int i=0 ; i < data.length ; i++)
        {
            shifted_data[ (i+shift) % data.length] = (-1) * factor * data[ i ];
        }

        for (int i=0 ; i < data.length ; i++)
        {
            result[i] = data[i] + shifted_data[i];
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
