package fact.features;

import fact.data.EventUtils;
import stream.Data;
import stream.Processor;

public class HillasConcentration2 implements Processor {

	private String shower = null;
	private String weights = null;
	private String outputKey = "HillasConcentration2";
	private String size = "Size";

	@Override
	public Data process(Data input) {
		if(!(	EventUtils.isKeyValid(input, shower, int[].class)
				&& EventUtils.isKeyValid(input, weights, float[].class)
				)){
			return null;
		}
		
		int[] 	showerPixel = (int[])input.get(shower);
		float[] photonCharge = (float[]) input.get(weights);
		
		float max_photon_charge                 = 0;
		float second_max_photon_charge          = 0;

		for (int pix : showerPixel)
		{
			if (photonCharge[pix] > max_photon_charge)
			{
				second_max_photon_charge        = max_photon_charge;
				max_photon_charge               = photonCharge[pix];
			}
			else if (photonCharge[pix] > second_max_photon_charge)
			{
				second_max_photon_charge    = photonCharge[pix];
			}

		}
		float sizeValue = 0;
		if(EventUtils.isKeyValid(input, size, Float.class))
		{
			sizeValue = (Float)input.get(size);
		}
		else
		{
			return null;
		}

		input.put(outputKey , (max_photon_charge + second_max_photon_charge) / sizeValue);
		
		return input;
	}
	
	public String getShower() {
		return shower;
	}

	public void setShower(String shower) {
		this.shower = shower;
	}

	public String getWeights() {
		return weights;
	}

	public void setWeights(String weights) {
		this.weights = weights;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}
}
