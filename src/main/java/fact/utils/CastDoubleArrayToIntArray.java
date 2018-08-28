package fact.utils;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class CastDoubleArrayToIntArray implements Processor {

    @Parameter(
            required = true,
            description = "Key to your double array."
    )
    private String inputKey;

    @Parameter(
            required = true,
            description = "Key to the output integer array."
    )
    protected String outputKey;

    @Override
    public Data process(Data item) {
        double[] doubleArray = (double[]) item.get(inputKey);
        int[] intArray = new int[doubleArray.length];

        for (int i = 0; i < intArray.length; ++i) {
            intArray[i] = (int) Math.round(doubleArray[i]);
        }

        item.put(outputKey, intArray);
        return item;
    }
}
