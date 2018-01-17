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
    public String key;

    @Parameter(required = true)
    public String outputKey;

    @Parameter(required = true)
    public int shift = 10;

    @Parameter
    public double factor = 0.66;


    @Override
    public Data process(Data item) {

        Utils.isKeyValid(item, key, double[].class);
        double[] data = (double[]) item.get(key);
        double[] shifted_data = new double[data.length];
        double[] result = new double[data.length];


        for (int i = 0; i < data.length; i++) {
            shifted_data[(i + shift) % data.length] = (-1) * factor * data[i];
        }

        for (int i = 0; i < data.length; i++) {
            result[i] = data[i] + shifted_data[i];
        }

        item.put(outputKey, result);

        return item;
    }
}
