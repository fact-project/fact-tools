package fact.features;

import stream.AbstractProcessor;
import stream.Data;
import stream.data.DataFactory;

import java.io.Serializable;
import java.util.Map;

/**
 * Create a new data item which has all its keys as lower case.
 * Created by alexey on 21.04.15.
 */
public class KeysToLower extends AbstractProcessor {

    @Override
    public Data process(Data data) {
        Data item = DataFactory.create();
        for(Map.Entry<String, Serializable> entry : data.entrySet()){
            item.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        return item;
    }
}
