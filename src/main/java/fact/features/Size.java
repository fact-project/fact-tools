package fact.features;

import stream.Data;
import stream.Processor;
import fact.EventUtils;
import stream.annotations.Parameter;

/**
 * Calculate the feature called Size. A physicist would call this the number of Photons in a shower. 
 * This basically sums up all weights that belong to a shower.
 * In short size is the sum of the photonCharge of all showerPixel. 
 * @author kaibrugge
 *
 */
public class Size implements Processor {

    @Parameter(required = true)
	private String showerKey;
    @Parameter(required = true)
	private String photonChargeKey;
    @Parameter(required = true)
	private String outputKey;

	@Override
	public Data process(Data input) {
		EventUtils.mapContainsKeys(getClass(), input, showerKey, photonChargeKey);
		
		int[] shower 	= (int[])input.get(showerKey);
		double[] charge 	= (double[])input.get(photonChargeKey);
		
		double size = 0;
		for (int i = 0; i < shower.length; i++){
			size += charge[shower[i]];
		}
		input.put(outputKey, size);
		return input;
	}
	

	public String getOutputKey() {
		return outputKey;
	}
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	
	public String getShowerKey() {
		return showerKey;
	}
	public void setShowerKey(String showerKey) {
		this.showerKey = showerKey;
	}

	public String getPhotonChargeKey() {
		return photonChargeKey;
	}
	public void setPhotonChargeKey(String photonChargeKey) {
		this.photonChargeKey = photonChargeKey;
	}

}
