package fact.features;

import fact.Utils;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
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


        Utils.mapContainsKeys(input, photonChargeKey, pixelSetKey);

        PixelSet pixelSet = (PixelSet) input.get(pixelSetKey);
        double[] charge = (double[]) input.get(photonChargeKey);
        double size = calculateSize(pixelSet, charge);
        input.put(outputKey, size);
        return input;
    }

    /**
     * Get the size of the shower.
     *
     * @param pixelSet PixelSet to take into account
     * @param weight some sort of weight for each pixel. Should have 1440 entries
     * @return the weighted sum of the showerpixels
     */
    public double calculateSize(PixelSet pixelSet, double[] weight) {
        double size = 0;
        for (CameraPixel pixel: pixelSet) {
            size += weight[pixel.id];
        }
        return size;
    }
}
