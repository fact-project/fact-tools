package fact.features;

import fact.Utils;
import fact.container.PixelSet;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Calculate the feature called Size. A physicist would call this the number of Photons in a shower.
 * This basically sums up all weights that belong to a shower.
 * In short size is the sum of the photonCharge of all showerPixel.
 *
 * @author kaibrugge
 */
public class Size implements Processor {

    @Parameter(required = true)
    public String pixelSetKey;

    @Parameter(required = true)
    public String photonChargeKey;

    @Parameter(required = true)
    public String outputKey;

    @Override
    public Data process(Data input) {


        Utils.mapContainsKeys(input, photonChargeKey);

        double size = 0;
        if (input.containsKey(pixelSetKey)) {

            int[] shower = ((PixelSet) input.get(pixelSetKey)).toIntArray();
            double[] charge = (double[]) input.get(photonChargeKey);

            size = calculateSize(shower, charge);
        }
        input.put("@size", size);
        input.put(outputKey, size);
        return input;
    }

    /**
     * Get the size of the shower.
     *
     * @param shower the array containing the chids of the pixels which are marked as showers
     * @param weight some sort of weight for each pixel. Should have 1440 entries
     * @return the weighted sum of the showerpixels
     */
    public double calculateSize(int[] shower, double[] weight) {
        double size = 0;
        for (int i = 0; i < shower.length; i++) {
            size += weight[shower[i]];
        }
        return size;
    }
}
