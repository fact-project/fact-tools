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
 * @author kaibrugge
 *
 */
public class Size implements Processor {

    @Parameter(required = false, description = "Key to the pixelSet, on which Size should be calculated", defaultValue = "pixels")
	private String pixelSetKey = "pixels";
    @Parameter(required = false, description = "Key to the estimated number of Photons", defaultValue = "pixels:estNumPhotons")
	private String estNumPhotonsKey = "pixels:estNumPhotons";
    @Parameter(required = false, description = "The outputkey, if not given, '<pixelset>:Size' is used")
	private String outputKey = null;

	@Override
	public Data process(Data item) {

		if (outputKey == null){
			outputKey = pixelSetKey + ":Size";
		}
		
		
		Utils.mapContainsKeys( item, estNumPhotonsKey);
		
		double size = 0;
		if (item.containsKey(pixelSetKey))
		{
		
			int[] shower = ((PixelSet) item.get(pixelSetKey)).toIntArray();
			double[] charge = (double[]) item.get(estNumPhotonsKey);

        	size = calculateSize(shower, charge);
		}
		item.put(outputKey, size);
		return item;
	}

    /**
     *Get the size of the shower.
     * @param shower the array containing the chids of the pixels which are marked as showers
     * @param weight some sort of weight for each pixel. Should have 1440 entries
     * @return the weighted sum of the showerpixels
     */
    public double calculateSize(int[] shower, double[] weight) {
        double size = 0;
        for (int i = 0; i < shower.length; i++){
            size += weight[shower[i]];
        }
        return size;
    }
}
