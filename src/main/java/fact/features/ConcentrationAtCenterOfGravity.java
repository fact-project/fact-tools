package fact.features;

import fact.mapping.FactCameraPixel;
import fact.mapping.FactPixelMapping;
import fact.viewer.ui.DefaultPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;



public class ConcentrationAtCenterOfGravity implements Processor
{
	FactPixelMapping pixelMap = FactPixelMapping.getInstance();
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
			hillasSizeValue = (Double) input.get(hillasSize);
			
			photonChargeArray = (double[]) input.get(photonCharge);
		}
		catch (ClassCastException e)
		{
			log.error("wrong types" + e.toString());
		}
		if(photonChargeArray == null || cogXValue == null || cogYValue == null)
		{
			log.error("Map does not contain the right values for the keys");
			return null;
		}
		
		// Assuming the correctness of function geomToChid !
		FactCameraPixel cogPixel = pixelMap.getPixelBelowCoordinatesInMM(cogXValue, cogYValue);
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
			double dist = (cogXValue - x) * (cogXValue - x) + (cogYValue - y) * (cogYValue - y);
			
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
		
		double conc = photonChargeArray[cogPixel.id] + photonChargeArray[minChId1.id] + photonChargeArray[minChId2.id];
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
	private Double hillasSizeValue = null;
	
	private double[] photonChargeArray = null;
	
	private String photonCharge;
	private String cogX;
	private String cogY;
	private String hillasSize;
	private String outputKey;
	
}