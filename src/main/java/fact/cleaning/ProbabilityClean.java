package fact.cleaning;

import java.util.ArrayList;
import java.util.List;

import fact.Constants;
import fact.EventUtils;
import fact.mapping.FactPixelMapping;
import stream.Data;
import stream.Processor;

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
		EventUtils.mapContainsKeys(getClass(), input, photonChargeKey, deltaKey);
		
		photoncharge = (double[]) input.get(photonChargeKey);
		
		delta = (Double) input.get(deltaKey);
		
		cogx = (Double) input.get(cogxKey);
		cogy = (Double) input.get(cogyKey);
		
		List<Integer> showerlist = new ArrayList<Integer>();
		
		for (int px = 0 ; px < Constants.NUMBEROFPIXEL ; px++)
		{
			double xpos = pixelMap.getPixelFromId(px).getXPositionInMM();
			double ypos = pixelMap.getPixelFromId(px).getYPositionInMM();
			double weight = photoncharge[px] / CalculateDistance(px,xpos,ypos);
			
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

	private double CalculateDistance(int px, double xpos, double ypos) {
		
		return 1;
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
