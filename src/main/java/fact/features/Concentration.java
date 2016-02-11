package fact.features;

import fact.Utils;
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
	private String estNumPhotonsKey = "pixels:estNumPhotons";
    @Parameter(required = false)
	private String outputKey = "shower:concentration:one";

	@Override
	public Data process(Data input) {
		Utils.mapContainsKeys(input, pixelSetKey, estNumPhotonsKey);
		PixelSet showerPixel;

		double[] estNumPhotons;
		try{
			 showerPixel = (PixelSet) input.get(pixelSetKey);
			 estNumPhotons = (double[]) input.get(estNumPhotonsKey);
		} catch (ClassCastException e){
			log.error("Could  not cast the keys to the right types");
			throw e;
		}
		if(showerPixel == null || showerPixel.set.size() == 0){
			log.warn("No shower in event. not calculating conenctration");
			return input;
		}
		
		//concentration according to F.Temme
		double maxWeight = 0;
		double secondMaxWeight = 0;
		double size = 0;
		
		for (CameraPixel pix : showerPixel.set)
		{
			size += estNumPhotons[pix.id];
			if (estNumPhotons[pix.id] > maxWeight)
			{
				secondMaxWeight        = maxWeight;
				maxWeight               = estNumPhotons[pix.id];
			}
			else if (estNumPhotons[pix.id] > secondMaxWeight)
			{
				secondMaxWeight    = estNumPhotons[pix.id];
			}

		}
		
		input.put(outputKey + ":one" , maxWeight / size);
		input.put(outputKey + ":two" , (maxWeight + secondMaxWeight) / size);
		return input;
	}

}
