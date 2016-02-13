package fact.features;

import fact.hexmap.CameraPixel;
import fact.container.PixelSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class Concentration implements Processor {
	static Logger log = LoggerFactory.getLogger(Concentration.class);

    @Parameter(required = true)
	private String pixelSetKey;
    @Parameter(required = true)
	private String weights;
    @Parameter(required = true)
	private String concOneOutputKey;
    @Parameter(required = true)
	private String concTwoOutputKey;

	@Override
	public Data process(Data input) {

		PixelSet showerPixel;
		double[] photonCharge;
		try{
			 showerPixel = (PixelSet) input.get(pixelSetKey);
			 photonCharge = (double[]) input.get(weights);
		} catch (ClassCastException e){
			log.error("Could  not cast the keys to the right types");
			throw e;
		}
		if(showerPixel == null || showerPixel.set.size() == 0){
			log.warn("No shower in event. not calculating conenctration");
			return input;
		}

		
		//concentration according to F.Temme
		double max_photon_charge                 = 0;
		double second_max_photon_charge          = 0;

		double size = 0;
		
		for (CameraPixel pix : showerPixel.set)
		{
			size += photonCharge[pix.id];
			if (photonCharge[pix.id] > max_photon_charge)
			{
				second_max_photon_charge        = max_photon_charge;
				max_photon_charge               = photonCharge[pix.id];
			}
			else if (photonCharge[pix.id] > second_max_photon_charge)
			{
				second_max_photon_charge    = photonCharge[pix.id];
			}

		}
		
		input.put(concOneOutputKey , max_photon_charge / size);
		input.put(concTwoOutputKey , (max_photon_charge + second_max_photon_charge) / size);
		return input;
	}

	public void setPixelSetKey(String pixelSetKey) {
		this.pixelSetKey = pixelSetKey;
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
