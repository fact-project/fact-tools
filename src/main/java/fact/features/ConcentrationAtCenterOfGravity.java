package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.viewer.ui.DefaultPixelMapping;



public class ConcentrationAtCenterOfGravity implements Processor
{
	
	/**
	 * This function calculates the concentration at the center of gravity including the 2 nearest pixel
	 */
	@Override
	public Data process(Data input)
	{
		try
		{
			cogXValue = (Float) input.get(cogX);
			cogYValue = (Float) input.get(cogY);
			hillasSizeValue = (Float) input.get(hillasSize);
			
			photonChargeArray = (float[]) input.get(photonCharge);
		}
		catch (ClassCastException e)
		{
			log.error("wrong types" + e.toString());
		}
		if(photonChargeArray == null || cogXValue == null || cogYValue == null)
		{
			log.error("Map does not conatin the right values for the keys");
			return null;
		}
		
		// Assuming the correctness of function geomToChid !
		int cogChId = DefaultPixelMapping.geomToChid(cogXValue, cogYValue);
		int[] neighbors = DefaultPixelMapping.getNeighborsFromChid(cogChId);
		
		// mindist1 < mindist2
		float mindist1 = Float.MAX_VALUE;
		float mindist2 = Float.MAX_VALUE;
		
		int minChId1 = cogChId;
		int minChId2 = cogChId;
		
		// search for the two nearest neighbors
		for (int pix : neighbors)
		{
			float x = DefaultPixelMapping.getGeomX(pix);
			float y = DefaultPixelMapping.getGeomY(pix);
			float dist = (cogXValue - x) * (cogXValue - x) + (cogYValue - y) * (cogYValue - y);
			
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
		
		float conc = photonChargeArray[cogChId] + photonChargeArray[minChId1] + photonChargeArray[minChId2];
		conc /= hillasSizeValue;
		input.put(outputKey, conc);
		
		return input;
	}
	
	

	public String getPhotonCharge() {
		return photonCharge;
	}
	@Parameter(required = true, defaultValue = "photoncharge", description = "Key of the array of photoncharge.")
	public void setPhotonCharge(String photonCharge) {
		this.photonCharge = photonCharge;
	}
	public String getCogX() {
		return cogX;
	}
	@Parameter(required = true, defaultValue = "COGx", description = "Key of the X-center of gravity of shower. (generate by e.g. Distribution from shower)")
	public void setCogX(String cogX) {
		this.cogX = cogX;
	}
	public String getCogY() {
		return cogY;
	}
	@Parameter(required = true, defaultValue = "COGy", description = "Key of the Y-center of gravity. (see CogX)")
	public void setCogY(String cogY) {
		this.cogY = cogY;
	}
	public String getOutputKey() {
		return outputKey;
	}
	@Parameter(required = true, defaultValue = "concCOG", description = "The key of the generated value.")
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}
	public String getHillasSize() {
		return hillasSize;
	}
	@Parameter(required = true, defaultValue  = "Hillas_size", description = "Key of the size of the event. (Generated e.g. by Size processor.)")
	public void setHillasSize(String hillasSize) {
		this.hillasSize = hillasSize;
	}



	static Logger log = LoggerFactory.getLogger(ConcentrationAtCenterOfGravity.class);
	
	private Float cogXValue = null;
	private Float cogYValue = null;
	private Float hillasSizeValue = null;
	
	private float[] photonChargeArray = null;
	
	private String photonCharge;
	private String cogX;
	private String cogY;
	private String hillasSize;
	private String outputKey;
	
}