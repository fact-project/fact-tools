package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;

public class HillasConcentration implements Processor {
	static Logger log = LoggerFactory.getLogger(HillasConcentration.class);

	private String shower = null;
	private String weights = null;
	private String outputKey = null;

	@Override
	public Data process(Data input) {
//		EventUtils.mapContainsKeys(getClass(), input, shower, weights);
	
		int[] 	showerPixel;
		double[] photonCharge;
		try{
			 showerPixel = (int[])input.get(shower);
			 photonCharge = (double[]) input.get(weights);
		} catch (ClassCastException e){
			log.error("Could  not cast the keys to the right types");
			throw e;
		}
		if(showerPixel == null || showerPixel.length == 0){
			log.warn("No shower in event. not calculating conenctration");
			return input;
		}

		
		//concentration according to F.Temme
		double max_photon_charge                 = 0;
		double second_max_photon_charge          = 0;

		double size = 0;
		
		for (int pix : showerPixel)
		{
			size += photonCharge[pix];
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
		
		input.put(outputKey+"_onePixel" , max_photon_charge / size);
		input.put(outputKey+"_twoPixel" , (max_photon_charge + second_max_photon_charge) / size);
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
