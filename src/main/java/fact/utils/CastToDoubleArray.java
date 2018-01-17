package fact.utils;

import fact.Utils;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

/**
 * Created by kaibrugge on 01.09.14.
 */
public class CastToDoubleArray implements StatefulProcessor {

    @Parameter(required = true, description = "The key to your data array.")
    public String key;

    @Parameter(required = false)
    public String outputKey;

    @Override
    public void init(ProcessContext context) throws Exception {
        if (outputKey == null) {
            outputKey = key;
        }
    }

    @Override
    public void resetState() throws Exception {
    }

    @Override
    public void finish() throws Exception {
    }

    @Override
    public Data process(Data item) {
        double[] b = Utils.toDoubleArray(item.get(key));
        item.put(outputKey, b);
        return item;
    }
}
