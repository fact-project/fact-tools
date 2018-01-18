package fact.utils;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class ElementwiseMultiplyDoubleArray implements Processor {

    @Parameter(
            required = true,
            description = "Key to your double array."
    )
    public String inputKey;

    @Parameter(
            required = true,
            description = "Key to the output double array."
    )
    public String outputKey;

    @Parameter(
            required = true,
            description = "Factor to multiply with."
    )
    public double factor;


    @Override
    public Data process(Data item) {
        double[] doubleArray = (double[]) item.get(inputKey);
        item.put(outputKey, ElementWise.multiply(doubleArray, factor));
        return item;
    }
}
