package fact.features;


import fact.Constants;
import fact.Utils;
import fact.mapping.FactCameraPixel;
import fact.mapping.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;


public class ConcentrationCore implements Processor{
	static Logger log = LoggerFactory.getLogger(ConcentrationCore.class);
	
	@Override
	public Data process(Data input)
	{

		Utils.mapContainsKeys(getClass(), input, cogX, cogY, delta, photonCharge, showerPixel, length, width, size);
		
		try{
			Double cogx = (Double) input.get(cogX);
			Double cogy = (Double) input.get(cogY);
			Double d = (Double) input.get(delta);
			double [] photonChargeArray = (double[]) input.get(photonCharge);
			int [] showerPixelArray = (int[]) input.get(showerPixel);
			Double l = (Double) input.get(length);
			Double w = (Double) input.get(width);
			Double hillasSize = (Double) input.get(size);
			
			double c = Math.cos(d);
			double s = Math.sin(d);
			
			double concCore = 0;
			
			for(int pix : showerPixelArray)
			{
                FactCameraPixel p = (FactCameraPixel) FactPixelMapping.getInstance().getPixelFromId(pix);
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
					 concCore += photonChargeArray[pix];
				
			}
			concCore /= hillasSize;
			input.put(outputKey, concCore);
			return input;
			
		} catch (ClassCastException e){
			log.error("Could not cast the values to the right types");
			throw e;
		}

	}
	
	public String getCogX() {
		return cogX;
	}
	@Parameter(required = true, defaultValue = "COGx", description  = "Key of the Center of Gravity X (by Distribution from shower)")
	public void setCogX(String cogX) {
		this.cogX = cogX;
	}
	public String getCogY() {
		return cogY;
	}

	@Parameter(required = true, defaultValue = "COGy", description  = "Key of the Center of Gravity Y (by Distribution from shower)")
	public void setCogY(String cogY) {
		this.cogY = cogY;
	}
	public String getDelta() {
		return delta;
	}

	@Parameter(required = true, defaultValue = "Hillas_Delta", description  = "Key of the Hillas delta angle")
	public void setDelta(String delta) {
		this.delta = delta;
	}
	public String getSize() {
		return size;
	}

	@Parameter(required = true, defaultValue = "Hillas_Size", description  = "Key of the Hillas size")
	public void setSize(String size) {
		this.size = size;
	}
	public String getPhotonCharge() {
		return photonCharge;
	}

	@Parameter(required = true, defaultValue = "photoncharge", description  = "Key of the photoncharge array")
	public void setPhotonCharge(String photonCharge) {
		this.photonCharge = photonCharge;
	}
	public String getShowerPixel() {
		return showerPixel;
	}

	@Parameter(required = true, defaultValue = "showerPixel", description  = "Key of the shower pixel array")
	public void setShowerPixel(String showerPixel) {
		this.showerPixel = showerPixel;
	}
	
	public String getWidth() {
		return width;
	}

	@Parameter(required = true, defaultValue = "Hillas_width", description  = "Key of the shower width")
	public void setWidth(String width) {
		this.width = width;
	}

	public String getLength() {
		return length;
	}
	
	@Parameter(required = true, defaultValue = "Hillas_length", description  = "Key of the shower length")
	public void setLength(String length) {
		this.length = length;
	}
	
	public String getOutputKey() {
		return outputKey;
	}

	@Parameter(required = true, defaultValue = "concCore", description  = "Key of the output value")
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	private String outputKey;
	private String cogX;
	private String cogY;
	private String delta;
	private String size;
	private String photonCharge;
	private String showerPixel;
	private String width;
	private String length;
	
	final private double pixelRadius = Constants.PIXEL_SIZE;
	
}
