package fact.features;

import fact.Utils;
import fact.mapping.FactCameraPixel;
import fact.mapping.FactPixelMapping;

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
	
	@Parameter(required = true, defaultValue = "photonCharge", description = "Key of the array of photoncharge.")
	private String photonChargeKey = null;
	@Parameter(required = true, defaultValue = "COGx", description = "Key of the X-center of gravity of shower. (generate by e.g. Distribution from shower)")
	private String cogxKey = null;
	@Parameter(required = true, defaultValue = "COGy", description = "Key of the Y-center of gravity. (see CogX)")
	private String cogyKey = null;
	@Parameter(required = true, defaultValue  = "Size", description = "Key of the size of the event. (Generated e.g. by Size processor.)")
	private String sizeKey = null;
	@Parameter(required = true, defaultValue = "concCOG", description = "The key of the generated value.")
	private String outputKey = null;
	
	private double cogx;
	private double cogy;
	private double size;
	
	private double[] photonCharge = null;
	

	/**
	 * This function calculates the concentration at the center of gravity including the 2 nearest pixel
	 */
	@Override
	public Data process(Data input)
	{
		Utils.mapContainsKeys(getClass(), input, cogxKey, cogyKey, sizeKey, photonChargeKey);
		
		cogx = (Double) input.get(cogxKey);
		cogy = (Double) input.get(cogyKey);
		size = (Double) input.get(sizeKey);
		
		photonCharge = (double[]) input.get(photonChargeKey);
		FactCameraPixel cogPixel = pixelMap.getPixelBelowCoordinatesInMM(cogx, cogy);
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


	public String getPhotonChargeKey() {
		return photonChargeKey;
	}


	public void setPhotonChargeKey(String photonChargeKey) {
		this.photonChargeKey = photonChargeKey;
	}


	public String getCogxKey() {
		return cogxKey;
	}


	public void setCogxKey(String cogxKey) {
		this.cogxKey = cogxKey;
	}


	public String getCogyKey() {
		return cogyKey;
	}


	public void setCogyKey(String cogyKey) {
		this.cogyKey = cogyKey;
	}


	public String getSizeKey() {
		return sizeKey;
	}


	public void setSizeKey(String sizeKey) {
		this.sizeKey = sizeKey;
	}


	public String getOutputKey() {
		return outputKey;
	}


	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}
	
	
}