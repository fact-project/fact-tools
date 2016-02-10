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

    @Parameter(required = false)
	private String pixelSetKey = "shower";
    @Parameter(required = false)
	private String weights = "pixels:numEstPhotons";

    @Parameter(required = false)
	private String outputKey = "shower:concentration:one";

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
		double maxWeight                 = 0;
		double secondMaxWeight          = 0;

		double size = 0;
		
		for (CameraPixel pix : showerPixel.set)
		{
			size += photonCharge[pix.id];
			if (photonCharge[pix.id] > maxWeight)
			{
				secondMaxWeight        = maxWeight;
				maxWeight               = photonCharge[pix.id];
			}
			else if (photonCharge[pix.id] > secondMaxWeight)
			{
				secondMaxWeight    = photonCharge[pix.id];
			}

		}
		
		input.put(outputKey + ":one" , maxWeight / size);
		input.put(outputKey + ":two" , (maxWeight + secondMaxWeight) / size);
		return input;
	}

}
