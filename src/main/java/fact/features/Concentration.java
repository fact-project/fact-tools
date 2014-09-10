package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class Concentration implements Processor {
	static Logger log = LoggerFactory.getLogger(Concentration.class);

    @Parameter(required = true)
	private String shower;
    @Parameter(required = true)
	private String weights;
    @Parameter(required = true)
	private String concOneOutputKey;
    @Parameter(required = true)
	private String concTwoOutputKey;

	@Override
	public Data process(Data input) {

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
		
		input.put(concOneOutputKey , max_photon_charge / size);
		input.put(concTwoOutputKey , (max_photon_charge + second_max_photon_charge) / size);
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

	public String getConcOneOutputKey() {
		return concOneOutputKey;
	}

	public void setConcOneOutputKey(String concOneOutputKey) {
		this.concOneOutputKey = concOneOutputKey;
	}

	public String getConcTwoOutputKey() {
		return concTwoOutputKey;
	}

	public void setConcTwoOutputKey(String concTwoOutputKey) {
		this.concTwoOutputKey = concTwoOutputKey;
	}


	
}
