package fact.features.evaluate;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;

public class CleaningEvaluate implements Processor {

    @Parameter(required = true)
    String showerKey = null;

    @Parameter(required = true)
    String mcCherenkovWeightKey = null;

    @Parameter(required = true)
    String mcNoiseWeightKey = null;

    @Parameter(required = true)
    String outputKey = null;
    int NumberOfSimulatedSlices = 2430; // Be aware that this is not the region of interest which was digitized, but the simulated region in ceres
    int integrationWindow = 30;
    double mcShowerThreshold = 2.0;
    private int npix;


    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");
        Utils.mapContainsKeys(input, showerKey, mcCherenkovWeightKey, mcNoiseWeightKey);

        int[] shower = (int[]) input.get(showerKey);
        double[] cherenkovWeight = Utils.toDoubleArray(input.get(mcCherenkovWeightKey));
        double[] noiseWeight = Utils.toDoubleArray(input.get(mcNoiseWeightKey));

        ArrayList<Integer> correctIdentifiedShowerPixel = new ArrayList<Integer>();
        ArrayList<Integer> wrongIdentifiedShowerPixel = new ArrayList<Integer>();
        ArrayList<Integer> notIdentifiedShowerPixel = new ArrayList<Integer>();

        for (int px = 0; px < npix; px++) {
            double cherSignalOverNoise = cherenkovWeight[px] / (noiseWeight[px] * integrationWindow / NumberOfSimulatedSlices);
            if (cherSignalOverNoise > mcShowerThreshold) {
                notIdentifiedShowerPixel.add(px);
            }
        }

        for (int i = 0; i < shower.length; i++) {
            int sh_px = shower[i];
            double cherSignalOverNoise = cherenkovWeight[sh_px] / (noiseWeight[sh_px] * integrationWindow / NumberOfSimulatedSlices);
            if (cherSignalOverNoise > mcShowerThreshold) {
                correctIdentifiedShowerPixel.add(sh_px);
                notIdentifiedShowerPixel.remove(sh_px);
            } else {
                wrongIdentifiedShowerPixel.add(sh_px);
            }
        }

        input.put(outputKey + "_correct", correctIdentifiedShowerPixel);
        input.put(outputKey + "_Numbercorrect", correctIdentifiedShowerPixel.size());
        input.put(outputKey + "_wrong", wrongIdentifiedShowerPixel);
        input.put(outputKey + "_Numberwrong", wrongIdentifiedShowerPixel.size());
        input.put(outputKey + "_not", notIdentifiedShowerPixel);
        input.put(outputKey + "_Numbernot", notIdentifiedShowerPixel.size());

        // TODO Auto-generated method stub
        return input;
    }
}
