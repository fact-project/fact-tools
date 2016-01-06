package fact.features;


import fact.Constants;
import fact.Utils;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import fact.container.PixelSetOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;


public class ConcentrationCore implements Processor{
	static Logger log = LoggerFactory.getLogger(ConcentrationCore.class);
	
	@Parameter(required=true)
	private String outputKey;
	@Parameter(required = true, description  = "Key of the Center of Gravity X (by Distribution from shower)")
	private String cogxKey;
	@Parameter(required = true, description  = "Key of the Center of Gravity Y (by Distribution from shower)")
	private String cogyKey;
	@Parameter(required = true, description  = "Key of the delta angle")
	private String deltaKey;
	@Parameter(required = true, description  = "Key of the sizeKey")
	private String sizeKey;
	@Parameter(required = true, description  = "Key of the photoncharge array")
	private String photonChargeKey;
	@Parameter(required = true, description  = "Key of the shower pixel array")
	private String pixelSetKey;
	@Parameter(required = true, description  = "Key of the shower width")
	private String widthKey;
	@Parameter(required = true, description  = "Key of the shower lengthKey")
	private String lengthKey;
	
	final private double pixelRadius = Constants.PIXEL_SIZE;
	
	public Data process(Data input)
	{

		Utils.mapContainsKeys( input, cogxKey, cogyKey, deltaKey, photonChargeKey, pixelSetKey, lengthKey, widthKey, sizeKey);
		
		try{
			Double cogx = (Double) input.get(cogxKey);
			Double cogy = (Double) input.get(cogyKey);
			Double d = (Double) input.get(deltaKey);
			double [] photonChargeArray = (double[]) input.get(photonChargeKey);
			PixelSetOverlay showerPixelArray = (PixelSetOverlay) input.get(pixelSetKey);
			Double l = (Double) input.get(lengthKey);
			Double w = (Double) input.get(widthKey);
			Double size = (Double) input.get(sizeKey);
			
			double c = Math.cos(d);
			double s = Math.sin(d);
			
			double concCore = 0;
			
			for(CameraPixel pix : showerPixelArray.set)
			{
                FactCameraPixel p = (FactCameraPixel) FactPixelMapping.getInstance().getPixelFromId(pix.id);
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

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public String getCogxKey() {
		return cogxKey;
	}

	public void setCogxKey(String cogxKey) {
		this.cogxKey = cogxKey;
	}

	public String getCogyKey() {
		return cogyKey;
	}

	public void setCogyKey(String cogyKey) {
		this.cogyKey = cogyKey;
	}

	public String getDeltaKey() {
		return deltaKey;
	}

	public void setDeltaKey(String deltaKey) {
		this.deltaKey = deltaKey;
	}

	public String getSizeKey() {
		return sizeKey;
	}

	public void setSizeKey(String sizeKey) {
		this.sizeKey = sizeKey;
	}

	public String getPhotonChargeKey() {
		return photonChargeKey;
	}

	public void setPhotonChargeKey(String photonChargeKey) {
		this.photonChargeKey = photonChargeKey;
	}

	public void setPixelSetKey(String pixelSetKey) {
		this.pixelSetKey = pixelSetKey;
	}

	public String getWidthKey() {
		return widthKey;
	}

	public void setWidthKey(String widthKey) {
		this.widthKey = widthKey;
	}

	public String getLengthKey() {
		return lengthKey;
	}

	public void setLengthKey(String lengthKey) {
		this.lengthKey = lengthKey;
	}
	

	
}
