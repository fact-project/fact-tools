package fact.features;

import fact.Constants;
import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This processor calculates the arrival time distribution per patch for trigger simulation.
 *
 * @author jan
 */
public class PerPatchArrivalTimeDistribution implements Processor {



    @Parameter(required = true, description = "Key to an arrivaltime array.", defaultValue = "arrivalTime")
    String key;

    @Parameter(required = true, description = "Outputkey", defaultValue = "perPatchArrivalTime")
    String outputKey;


    @Override
    public Data process(Data item) {
        Utils.mapContainsKeys(item, key);

        double[] arrivalTimeArray = Utils.toDoubleArray(item.get(key));
        double[] perPatchMean = new double[Constants.N_PIXELS / 9];
        double[] perPatchVariance = new double[Constants.N_PIXELS / 9];

        for (int chid = 0; chid < Constants.N_PIXELS; chid++) {
            int patch = chid / 9;
            perPatchMean[patch] += arrivalTimeArray[chid] / 9.0;
        }
        for (int chid = 0; chid < Constants.N_PIXELS; chid++) {
            int patch = chid / 9;
            perPatchVariance[patch] += (arrivalTimeArray[chid] - perPatchMean[patch]) * (arrivalTimeArray[chid] - perPatchMean[patch]) / 8.0;
        }

        item.put(outputKey + "_mean", perPatchMean);
        item.put(outputKey + "_var", perPatchVariance);
        return item;
    }

}
