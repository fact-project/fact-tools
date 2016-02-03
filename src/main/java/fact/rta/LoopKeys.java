package fact.rta;

import stream.Data;
import stream.Keys;
import stream.ProcessorList;
import stream.annotations.Parameter;

import java.io.Serializable;

/**
 * Created by kai on 02.02.16.
 */
public class LoopKeys extends ProcessorList {

    @Parameter(required = true)
    private Keys keys = new Keys("*");

    @Parameter(required = true)
    private String targetKey = "Theta";


    @Override
    public Data process(Data data) {
        Serializable originalValue = data.remove(targetKey);
        for (String key : keys.select(data)) {
            Serializable offValue = data.get(key);
            data.put(targetKey, offValue);
            data = super.process(data);
        }
        //restore original state of the values
        data.put(targetKey, originalValue);
        return data;
    }

    public void setKeys(Keys keys) {
        this.keys = keys;
    }

    public void setTargetKey(String targetKey) {
        this.targetKey = targetKey;
    }
}
