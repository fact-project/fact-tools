package fact.utils;

import fact.Constants;
import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;

/**
 * Created by jbuss on 20.11.14.
 */
public class SelectArrayByChid implements Processor {
    static Logger log = LoggerFactory.getLogger(SelectArrayByChid.class);
    @Parameter(required = true, description = "Key to the array you want the information about")
    private String key = null;
    @Parameter(required = true, description = "The name of the data written to the stream")
    private String outputKey = null;
    @Parameter(description = "Pixel ID of a desired Pixel")
    private int chid = 0;

    @Override
    public Data process(Data input) {

        Utils.mapContainsKeys(input, key);

        double[][] data = (double[][]) input.get(key);

        //add processors threshold to the DataItem
        input.put(outputKey, data[chid]);

        return input;
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

    public int getChid() {
        return chid;
    }

    public void setChid(int chid) {
        this.chid = chid;
    }
}
