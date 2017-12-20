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
    public Data process(Data input) {
        double[] doubleArray = (double[]) input.get(inputKey);
        input.put(outputKey, ElementWise.multiply(doubleArray, factor));
        return input;
    }
}
