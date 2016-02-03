package fact.rta;

import stream.Data;
import stream.Keys;
import stream.ProcessorList;
import stream.annotations.Parameter;

import java.io.Serializable;

/**
 * Created by kai on 02.02.16.
 */
public class LoopOffPositions extends ProcessorList {

    @Parameter(required = false)
    private Keys offKeys = new Keys("Theta_Off_?");

    @Parameter(required = false)
    private String targetKey = "Theta";


    @Override
    public Data process(Data data) {
        Serializable originalValue = data.remove(targetKey);
        for (String key : offKeys.select(data)) {
            Serializable offValue = data.get(key);
            data.put(targetKey, offValue);
            data = super.process(data);
        }
        //restore original state of the values
        data.put(targetKey, originalValue);
        return data;
    }

    public void setOffKeys(Keys offKeys) {
        this.offKeys = offKeys;
    }

    public void setTargetKey(String targetKey) {
        this.targetKey = targetKey;
    }
}
