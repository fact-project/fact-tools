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


public class ConcentrationCore implements Processor{
	static Logger log = LoggerFactory.getLogger(ConcentrationCore.class);
	
	@Parameter(required=false)
	private String outputKey = "shower:concentrationCore";
	@Parameter(required=false)
	private String ellipseKey = "shower:ellipse";

	@Parameter(required = false, description  = "Key to the size feature")
	private String sizeKey = "shower:size";
	@Parameter(required = false, description  = "Key of the photoncharge array")
	private String estNumPhotonsKey = "pixels:estNumPhotons";
	@Parameter(required = false, description  = "Key of the shower pixel array")
	private String pixelSetKey = "shower";

	
	final private double pixelRadius = Constants.PIXEL_SIZE;
	
	public Data process(Data input)
	{

		Utils.mapContainsKeys( input, estNumPhotonsKey, pixelSetKey, ellipseKey, sizeKey);

		try{
			Double cogx = (Double) input.get(pixelSetKey + ":cog:x");
			Double cogy = (Double) input.get(pixelSetKey + ":cog:y");

            Double d = (Double) input.get(ellipseKey + ":delta");
			double [] photonChargeArray = (double[]) input.get(estNumPhotonsKey);
			PixelSet showerPixelArray = (PixelSet) input.get(pixelSetKey);
			Double l = (Double) input.get(ellipseKey + ":length");
			Double w = (Double) input.get(ellipseKey + ":width");
			Double size = (Double) input.get(sizeKey);
			
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
			input.put(outputKey, concCore);
			return input;
			
		} catch (ClassCastException e){
			log.error("Could not cast the values to the right types");
			throw e;
		}

	}

	

	
}
