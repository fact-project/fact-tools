package fact.features;

import fact.data.EventUtils;
import stream.Data;
import stream.Processor;
/**
 * Sum up all the weights for pixel between the max and min values.
 * The output of this processor is the sum of the pixel weights in the shower array iff the weight is > min and < max.
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 *
 */
public class SizeInInterval implements Processor {

	private String showerKey;
	private String photonChargeKey;
	private String outputKey;
	private float  min = 0;
	private float  max = 2048; //sensor limit?
	
	@Override
	public Data process(Data input) {

		EventUtils.mapContainsKeys(getClass(), input, showerKey, photonChargeKey);
		
		int[] shower 	= (int[])input.get(showerKey);
		float[] charge 	= (float[])input.get(photonChargeKey);
		
		float size = 0;
		for (int i = 0; i < shower.length; i++){
			if (shower[i] > min &&  shower[i] < max){
				size += charge[shower[i]];
			}
		}
		input.put(outputKey, size);
		System.out.println("Sizeininterval: " + size);
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

	public float getMin() {
		return min;
	}

	public void setMin(float min) {
		this.min = min;
	}

	public float getMax() {
		return max;
	}

	public void setMax(float max) {
		this.max = max;
	}

}
