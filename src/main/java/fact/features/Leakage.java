package fact.features;

import fact.Utils;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import fact.container.PixelSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class Leakage implements Processor {
	static Logger log = LoggerFactory.getLogger(Leakage.class);

    @Parameter(required = false)
	private String pixelSetKey = "shower";

    @Parameter(required = false)
	private String weightsKey = "pixels:estNumPhotons";

    @Parameter(required = false)
	private String outputKey= "shower:leakage";

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

	@Override
	public Data process(Data input) {
		Utils.mapContainsKeys( input, pixelSetKey, weightsKey);

		PixelSet showerPixel = (PixelSet) input.get(pixelSetKey);
		double[] photonCharge = (double[]) input.get(weightsKey);
		
		
		double size = 0;
	
	    double leakageBorder          = 0;
	    double leakageSecondBorder    = 0;

	    for (CameraPixel pix: showerPixel.set)
	    {
	    	size += photonCharge[pix.id];
	        if (isBorderPixel(pix.id) )
	        {
	            leakageBorder          += photonCharge[pix.id];
	            leakageSecondBorder    += photonCharge[pix.id];
	        }
	        else if (isSecondBorderPixel(pix.id))
	        {
	            leakageSecondBorder    += photonCharge[pix.id];
	        }
	    }
	    leakageBorder          = leakageBorder        / size;
	    leakageSecondBorder    = leakageSecondBorder  / size;

		
		input.put(outputKey + ":one", leakageBorder);
		input.put(outputKey + ":two", leakageSecondBorder);
		return input;
	}
	
	//this is of course not the most efficient solution
	boolean isSecondBorderPixel(int pix){
		for(FactCameraPixel nPix: pixelMap.getNeighboursFromID(pix))
		{
			if(isBorderPixel(nPix.id)){
				return true;
			}
		}
		return false;
	}
	boolean isBorderPixel(int pix){
        return pixelMap.getNeighboursFromID(pix).length < 6;
	}
}
