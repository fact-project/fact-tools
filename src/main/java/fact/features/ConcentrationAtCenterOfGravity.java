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

	@Parameter(required = false, description = "The key of the generated value.")
	private String outputKey = "shower:concentrationCOG";

	


	/**
	 * This function calculates the concentration at the center of gravity including the 2 nearest pixel
	 */
	@Override
	public Data process(Data item)
	{
		Utils.mapContainsKeys( item, pixelSetKey, photonChargeKey);
		
		double cogx = (Double) item.get(pixelSetKey + ":cog:x");
		double cogy = (Double) item.get(pixelSetKey + ":cog:y");
		double size = (Double) item.get(pixelSetKey + ":size");

        double[] photonCharge = (double[]) item.get(photonChargeKey);
		FactCameraPixel cogPixel = pixelMap.getPixelBelowCoordinatesInMM(cogx, cogy);
		if (cogPixel == null)
		{
			item.put(outputKey, -Double.MAX_VALUE);
			return item;
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
		item.put(outputKey, conc);
		
		return item;
	}
	
}