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
		if(EventUtils.isKeyValid(input, size, float.class))
		{
			sizeValue = (Float)input.get(size);
		}
		else
		{
			sizeValue = 0;
			for(float s: photonCharge) sizeValue += s; 
		}

		input.put(outputKey , (max_photon_charge + second_max_photon_charge) / sizeValue);
		
		return null;
	}

}
