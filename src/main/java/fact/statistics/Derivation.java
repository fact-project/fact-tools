package fact.statistics;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Return an array with the deviation of neighboring slices in each pixel.
 * Therefore loop over data array and subtract the prior slice from the current.
 * <p>
 * Created by ftemme, jbuss?
 */


public class Derivation implements Processor {
    static Logger log = LoggerFactory.getLogger(Derivation.class);

    @Parameter(required = true)
    String key = null;

    @Parameter(required = true)
    String outputKey = null;

    @Override
    public Data process(Data input) {
        Utils.mapContainsKeys(input, key);

        double[] data = (double[]) input.get(key);
        double[] result = new double[data.length];

        for (int i = 1; i < data.length; i++) {
            result[i] = data[i] - data[i - 1];
        }

        input.put(outputKey, result);

        return input;
    }
}
