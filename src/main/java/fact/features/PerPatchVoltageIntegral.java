package fact.features;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This processor calculates the per patch voltage integral for trigger simulation.
 */

public class PerPatchVoltageIntegral implements Processor {
    @Parameter(required = true, defaultValue = "dataCalibrated", description = "The input key")
    public String key = "";

    @Parameter(required = true, defaultValue = "perPatchVoltageIntegral", description = "The output key")
    public String outputKey;

    private int npix;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");
        Utils.mapContainsKeys(input, key);
        double[] dataCalibratedArray = (double[]) input.get(key);
        int roi = dataCalibratedArray.length / npix;
        double[] perPatchVoltageIntegral = new double[160];

        int patch = 0;

        for (int chid = 0; chid < npix; chid++) {
            for (int slice = 0; slice < roi; slice++) {
                patch = chid / 9;
                perPatchVoltageIntegral[patch] += 0.5 * dataCalibratedArray[chid * roi + slice]; // 0.5 ns per slice
            }
        }

        input.put(outputKey, perPatchVoltageIntegral);

        return input;
    }

}
