package fact.utils;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Created by kaibrugge on 15.10.14.
 */
public class SelectFromArray implements Processor {

    @Parameter(required = false, description = "The key to the array containing array indices")
    String indicesKey="";

    @Override
    public Data process(Data data) {
        Utils.isKeyValid(data,indicesKey, int[].class);
        int[] indices = (int[]) data.get(indicesKey);
        for(int i : indices){

        }

        return null;
    }
}
