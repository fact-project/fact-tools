package fact.features;


import fact.Constants;
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

import static fact.container.PixelSet.name;


public class ConcentrationCore implements Processor{
	static Logger log = LoggerFactory.getLogger(ConcentrationCore.class);
	
	@Parameter(required=false)
	private String outputKey = null;

	@Parameter(required = false, description  = "Key of the photoncharge array")
	private String estNumPhotonsKey = "pixels:estNumPhotons";

	@Parameter(required = false, description  = "Key of the shower pixel array")
	private String pixelSetKey = "shower";

	
	final private double pixelRadius = Constants.PIXEL_SIZE;
	
	public Data process(Data input)
	{

		Utils.mapContainsKeys( input, estNumPhotonsKey, pixelSetKey);
        String ellipse =  pixelSetKey + ":" + "ellipse";

		try{
			Double cogx = (Double) input.get(pixelSetKey + ":cog:x");
			Double cogy = (Double) input.get(pixelSetKey + ":cog:y");

            Double d = (Double) input.get(ellipse + ":delta");
			double [] photonChargeArray = (double[]) input.get(estNumPhotonsKey);
			PixelSet showerPixelArray = (PixelSet) input.get(pixelSetKey);
			Double l = (Double) input.get(ellipse + ":length");
			Double w = (Double) input.get(ellipse + ":width");
			Double size = (Double) input.get(pixelSetKey + ":size");
			
			double c = Math.cos(d);
			double s = Math.sin(d);
			
			double concCore = 0;
			
			for(CameraPixel pix : showerPixelArray.set)
			{
                FactCameraPixel p = FactPixelMapping.getInstance().getPixelFromId(pix.id);
				double px = p.getXPositionInMM();
				double py = p.getYPositionInMM();
				
				// short names adapted from mars code (change when understood)
				double dx = px - cogx;
				double dy = py - cogy;
				
				double dist0 = dx*dx + dy*dy;
				
				double dzx =  c * dx + s * dy;
				double dzy = -s * dx + c * dy;
				
				double rl = 1/(l * l);
				double rw = 1/(w * w);
				double dz = pixelRadius * pixelRadius / 4;

				double tana = dzy * dzy / (dzx * dzx);
				double distr = (1+tana)/(rl + tana*rw);
				
				if (distr>dist0-dz || dzx==0)
					 concCore += photonChargeArray[pix.id];
				
			}
			concCore /= size;
			input.put(name(outputKey, pixelSetKey, "concentrationCore"), concCore);
			return input;
			
		} catch (ClassCastException e){
			log.error("Could not cast the values to the right types");
			throw e;
		}

	}

	

	
}
