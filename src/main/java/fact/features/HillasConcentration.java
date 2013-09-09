package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import fact.data.EventUtils;

public class HillasConcentration implements Processor {
	static Logger log = LoggerFactory.getLogger(HillasConcentration.class);

	private String shower = null;
	private String weights = null;
	private String outputKey = "HillasConcentration";

	@Override
	public Data process(Data input) {
		
		if(!(	EventUtils.isKeyValid(input, shower, int[].class)
				&& EventUtils.isKeyValid(input, weights, float[].class)
				)){
			return null;
		}
	
		int[] 	showerPixel = (int[])input.get(shower);
		float[] photonCharge = (float[]) input.get(weights);

		
		//concentration according to F.Temme
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
		//----
		float size = 0;
		for(float s: photonCharge) size += s; 
		
		input.put(outputKey , max_photon_charge / size);
//		concentration2Pixel                         = (max_photon_charge + second_max_photon_charge) / size;
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
}
