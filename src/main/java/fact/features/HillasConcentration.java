package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import fact.EventUtils;

public class HillasConcentration implements Processor {
	static Logger log = LoggerFactory.getLogger(HillasConcentration.class);

	private String shower = null;
	private String weights = null;
	private String outputKeyConcentration = "HillasConcentration";
	private String outputKeyConcentration2 = "HillasConcentration2";

	@Override
	public Data process(Data input) {
		EventUtils.mapContainsKeys(getClass(), input, shower, weights);
	
		int[] 	showerPixel;
		double[] photonCharge;
		try{
			 showerPixel = (int[])input.get(shower);
			 photonCharge = (double[]) input.get(weights);
		} catch (ClassCastException e){
			log.error("Could  not cast the keys to the right types");
			throw e;
		}

		
		//concentration according to F.Temme
		double max_photon_charge                 = 0;
		double second_max_photon_charge          = 0;
		
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
		double size = 0;
		for(double s: photonCharge) size += s; 
		
		input.put(outputKeyConcentration , max_photon_charge / size);
		input.put(outputKeyConcentration2 , (max_photon_charge + second_max_photon_charge) / size);
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
		return outputKeyConcentration;
	}
	public void setOutputKey(String outputKey) {
		this.outputKeyConcentration = outputKey;
	}
}
