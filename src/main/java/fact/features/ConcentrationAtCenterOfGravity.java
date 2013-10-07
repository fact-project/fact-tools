package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import fact.viewer.ui.DefaultPixelMapping;

/**
 * This class calculates the concentration at the center of gavity including the 2 nearest pixel
 * @param cogx The key of the center of gravity x
 * @param cogy The key of the center of gravity y
 * @param hillasSize The key of the HillasSize of the event
 * @param PhotonCharge The photoncharge!
 *
 * @return ConcCOG The concentration of the 3 closest pixel to the COG 
 */
public class ConcentrationAtCenterOfGravity implements Processor
{
	
	
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
	public void setPhotonCharge(String photonCharge) {
		this.photonCharge = photonCharge;
	}
	public String getCogX() {
		return cogX;
	}
	public void setCogX(String cogX) {
		this.cogX = cogX;
	}
	public String getCogY() {
		return cogY;
	}
	public void setCogY(String cogY) {
		this.cogY = cogY;
	}
	public String getOutputKey() {
		return outputKey;
	}
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}
	public String getHillasSize() {
		return hillasSize;
	}
	public void setHillasSize(String hillasSize) {
		this.hillasSize = hillasSize;
	}



	static Logger log = LoggerFactory.getLogger(ConcentrationAtCenterOfGravity.class);
	
	private Float cogXValue = null;
	private Float cogYValue = null;
	private Float hillasSizeValue = null;
	
	private float[] photonChargeArray = null;
	
	private String photonCharge = "photoncharge";
	private String cogX = "COGx";
	private String cogY = "COGy";
	private String hillasSize = "Hillas_Size";
	private String outputKey = "ConcCOG";
	
}