package fact.utils;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.Arrays;

public class SortDoubleArray implements Processor {
    @Parameter(required = true, description = "Input key to 1 dimensional double array")
    public String key = "";

    @Parameter(required = true, description = "Outputkey")
    public String outputKey = "";

    @Override
    public Data process(Data item) {
        try {
            Utils.mapContainsKeys(item, key);
            double[] array = (double[]) item.get(key);
            Arrays.sort(array);
            item.put(outputKey, array);
        } catch (Exception e) {
            item.put(null, outputKey);
        }

        return item;
    }
}
