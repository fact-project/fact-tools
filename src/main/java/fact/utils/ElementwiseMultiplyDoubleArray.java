package fact.utils;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class ElementwiseMultiplyDoubleArray implements Processor {

    @Parameter(
            required = true,
            description = "Key to your double array."
    )
    private String inputKey;

    @Parameter(
            required = true,
            description = "Key to the output double array."
    )
    protected String outputKey;

    @Parameter(
            required = true,
            description = "Factor to multiply with."
    )
    protected double factor;


    @Override
    public Data process(Data input) {
        double[] doubleArray = (double[]) input.get(inputKey);
        input.put(outputKey, ElementWise.multiply(doubleArray, factor));
        return input;
    }

    public void setInputKey(String inputKey) {
        this.inputKey = inputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

}
