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
    private String key;
    @Parameter(required = false)
    private String outputKey;

    @Override
    public void init(ProcessContext context) throws Exception {
        if (outputKey == null){
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
    public Data process(Data input) {
        double[] b = Utils.toDoubleArray(input.get(key));
        input.put(outputKey, b);
        return input;
    }


    public void setKey(String key) {
        this.key = key;
    }
    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

}
