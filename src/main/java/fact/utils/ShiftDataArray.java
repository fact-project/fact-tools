package fact.utils;

import fact.Utils;
import fact.filter.MovingLinearFit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Created by jbuss on 09.10.14.
 */
public class ShiftDataArray implements Processor {
    static Logger log = LoggerFactory.getLogger(MovingLinearFit.class);

    @Parameter(required = true, description = "key of input array")
    public String key = null;

    @Parameter(required = true, description = "key of output array")
    public String outputKey = null;

    @Parameter(description = "shift of the array", defaultValue = "1")
    public int shift = 1;

    @Override
    public Data process(Data input) {
        Utils.mapContainsKeys(input, key);

        double[] data = (double[]) input.get(key);
        double[] result = new double[data.length];


        for (int i = 1; i < data.length; i++) {
            if (shift < 0) {
                result[(i + shift) % data.length] = data[i];
            } else {
                result[i] = data[(i + shift) % data.length];
            }
        }

        input.put(outputKey, result);

        return input;
    }
}
