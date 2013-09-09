package fact.features;

import fact.data.EventUtils;
import stream.Data;
import stream.Processor;

public class Size implements Processor {

	private String showerKey;
	private String photonChargeKey;
	private String outputKey;
	
	@Override
	public Data process(Data input) {

		if(!EventUtils.isKeyValid(input, showerKey, int[].class)){
			return null;
		}
		
		if(!EventUtils.isKeyValid(input, photonChargeKey, int[].class)){
			return null;
		}

		int[] shower 	= (int[])input.get(showerKey);
		float[] charge 	= (float[])input.get(photonChargeKey);
		
		float size = 0;
		for (int i = 0; i < shower.length; i++){
			size += charge[shower[i]];
		}
		input.put(outputKey, size);
		// TODO Auto-generated method stub
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
