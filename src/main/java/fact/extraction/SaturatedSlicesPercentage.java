package fact.extraction;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Simply calculates the percentage of values in an array, that are above a certain threshold.
 * Defaults are set so that it is calculated how many slices of the adc values are at the maximum adc value of
 * 2048. This should be useful to skip the 27s burst events induced by our broken lightpulser.
 */
public class SaturatedSlicesPercentage implements Processor {

    @Parameter
    public String inputKey = "Data";

    @Parameter
    public String outputKey = "saturated_slices_percentage";

    @Parameter
    public double threshold = 2048;

    @Override
    public Data process(Data data) {

        Utils.mapContainsKeys(data, inputKey);

        double[] array = Utils.toDoubleArray(data.get(inputKey));

        double slicesOverThresholdPercentage = 0;
        for (double value: array) {
            if (value >= threshold) {
                slicesOverThresholdPercentage += 1;
            }
        }
        slicesOverThresholdPercentage /= array.length;

        data.put(outputKey, slicesOverThresholdPercentage);

        return data;
    }
}
