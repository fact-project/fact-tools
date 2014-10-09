package fact.cleaning;

import fact.Constants;
import fact.Utils;
import fact.hexmap.FactPixelMapping;
import stream.Data;
import stream.Processor;

import java.util.ArrayList;
import java.util.List;

/**
 * Wolfgangs idea after previous cleaning make a new cleaning depending on the distance to showeraxis.
 * TODO: find thresholds and check code. unit test?
 * @author Fabian Temme
 */
public class ProbabilityClean implements Processor {

	private String photonChargeKey = null;
	
	private String outputKey = null;
	
	private String deltaKey = null;
	private String cogxKey = null;
	private String cogyKey = null;
	
	private double probabilityThreshold;
	
	private double[] photoncharge = null;
	private double delta;
	private double cogx;
	private double cogy;
	
	FactPixelMapping pixelMap = FactPixelMapping.getInstance();
	
	@Override
	public Data process(Data input) {
		Utils.mapContainsKeys(input, photonChargeKey, deltaKey);
		
		photoncharge = (double[]) input.get(photonChargeKey);
		
		delta = (Double) input.get(deltaKey);
		
		cogx = (Double) input.get(cogxKey);
		cogy = (Double) input.get(cogyKey);
		
		List<Integer> showerlist = new ArrayList<Integer>();
		
		for (int px = 0 ; px < Constants.NUMBEROFPIXEL ; px++)
		{
			double xpos = pixelMap.getPixelFromId(px).getXPositionInMM();
			double ypos = pixelMap.getPixelFromId(px).getYPositionInMM();
			double weight = photoncharge[px] / Utils.calculateDistancePointToShowerAxis(cogx, cogy, delta, xpos, ypos);
			
			if (weight > probabilityThreshold)
			{
				showerlist.add(px);
			}
			
		}
		if (showerlist.size() > 0)
		{
			Integer[] showerArray = new Integer[showerlist.size()];
			showerlist.toArray(showerArray);
			
			input.put(outputKey, showerArray);
		}
		
		return input;
	}

	public String getPhotonChargeKey() {
		return photonChargeKey;
	}

	public void setPhotonChargeKey(String photonChargeKey) {
		this.photonChargeKey = photonChargeKey;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public String getDeltaKey() {
		return deltaKey;
	}

	public void setDeltaKey(String deltaKey) {
		this.deltaKey = deltaKey;
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

	public double getProbabilityThreshold() {
		return probabilityThreshold;
	}

	public void setProbabilityThreshold(double probabilityThreshold) {
		this.probabilityThreshold = probabilityThreshold;
	}

}
