package fact.utils;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Created by jbuss on 20.11.14.
 */
public class SelectValueByChid implements Processor {
    static Logger log = LoggerFactory.getLogger(SelectValueByChid.class);

    @Parameter(required = true, description = "Key to the array you want the information about")
    public String key = null;

    @Parameter(required = true, description = "The name of the data written to the stream")
    public String outputKey = null;

    @Parameter(description = "key of an array containing the IDs of a desired Subset")
    public int chid = 0;

    @Override
    public Data process(Data item) {

        Utils.mapContainsKeys(item, key);

        double[] data = Utils.toDoubleArray(item.get(key));

        //add processors threshold to the DataItem
        if (data != null) {
            item.put(outputKey, data[chid]);
        }

        return item;
    }
}
