package fact.features;

import fact.Utils;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;


/**
 * 
 * @author Fabian Temme
 *
 */
public class ConcentrationAtCenterOfGravity implements Processor
{
	FactPixelMapping pixelMap = FactPixelMapping.getInstance();
	
	static Logger log = LoggerFactory.getLogger(ConcentrationAtCenterOfGravity.class);
	
	@Parameter(required = false, description = "Key of the array of photoncharge.")
	public  String photonChargeKey = "pixels:estNumPhotons";

	@Parameter(required = false)
	public String pixelSetKey = "shower";

	@Parameter(required = true, description = "The key of the generated value.")
	private String outputKey = "shower:concentrationCOG";

	
	private double[] photonCharge = null;
	

	/**
	 * This function calculates the concentration at the center of gravity including the 2 nearest pixel
	 */
	@Override
	public Data process(Data input)
	{
		Utils.mapContainsKeys( input, pixelSetKey, photonChargeKey);
		
		double cogx = (Double) input.get(pixelSetKey + ":cog:x");
		double cogy = (Double) input.get(pixelSetKey + ":cog:y");
		double size = (Double) input.get(pixelSetKey + ":size");
		
		photonCharge = (double[]) input.get(photonChargeKey);
		FactCameraPixel cogPixel = pixelMap.getPixelBelowCoordinatesInMM(cogx, cogy);
		if (cogPixel == null)
		{
			input.put(outputKey, -Double.MAX_VALUE);
			return input;
		}
		FactCameraPixel[] neighbors = pixelMap.getNeighboursForPixel(cogPixel);
		
		// mindist1 < mindist2
		double mindist1 = Float.MAX_VALUE;
		double mindist2 = Float.MAX_VALUE;
		
		FactCameraPixel minChId1 = cogPixel;
		FactCameraPixel minChId2 = cogPixel;
		
		// search for the two nearest neighbors
		for (FactCameraPixel pix : neighbors)
		{
			double x = pix.getXPositionInMM();
			double y = pix.getYPositionInMM();
			double dist = (cogx - x) * (cogx - x) + (cogy - y) * (cogy - y);
			
			if(dist < mindist1)
			{
				mindist2 = mindist1;
				mindist1 = dist;
				minChId2 = minChId1;
				minChId1 = pix;
			}else if (dist < mindist2)
			{
				mindist2 = dist;
				minChId2 = pix;
			}
		}
		
		double conc = photonCharge[cogPixel.id] + photonCharge[minChId1.id] + photonCharge[minChId2.id];
		conc /= size;
		input.put(outputKey, conc);
		
		return input;
	}
	
}