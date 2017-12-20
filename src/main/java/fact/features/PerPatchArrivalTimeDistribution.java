package fact.features;

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

    private int npix;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, "NPIX", Integer.class);
        Utils.mapContainsKeys(input, key);
        npix = (Integer) input.get("NPIX");

        double[] arrivalTimeArray = Utils.toDoubleArray(input.get(key));
        //try{
        double[] perPatchMean = new double[npix / 9];
        double[] perPatchVariance = new double[npix / 9];

        int patch = 0;
        for (int chid = 0; chid < npix; chid++) {

            patch = (int) chid / 9;
            perPatchMean[patch] += arrivalTimeArray[chid] / 9.0;
        }
        for (int chid = 0; chid < npix; chid++) {
            patch = chid / 9;
            perPatchVariance[patch] += (arrivalTimeArray[chid] - perPatchMean[patch]) * (arrivalTimeArray[chid] - perPatchMean[patch]) / 8.0;
        }

        input.put(outputKey + "_mean", perPatchMean);
        input.put(outputKey + "_var", perPatchVariance);
        /*}catch(Exception e)
		{
			input.put(outputKey + "_mean", null);
			input.put(outputKey + "_var", null);
			return input;
		}*/

        return input;
    }

}
