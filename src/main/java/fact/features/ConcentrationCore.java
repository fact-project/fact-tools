package fact.features;

import stream.Data;
import stream.Processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.annotations.Parameter;
import fact.Constants;
import fact.data.EventUtils;
import fact.features.video.CenterOfGravity;
import fact.statistics.PixelDistribution2D;
import fact.viewer.ui.DefaultPixelMapping;



public class ConcentrationCore implements Processor{

	@Override
	public Data process(Data input)
	{

		if(!EventUtils.isKeyValid(input, cogX, Double.class)){
			return null;
		}
		if(!EventUtils.isKeyValid(input, cogY, Double.class)){
			return null;
		}
		if(!EventUtils.isKeyValid(input, delta, Double.class)){
			return null;
		}
		if(!EventUtils.isKeyValid(input, photonCharge, double[].class)){
			return null;
		}		
		if(!EventUtils.isKeyValid(input, showerPixel, int[].class)){
			return null;
		}
		
		Double cogx = (Double) input.get(cogX);
		Double cogy = (Double) input.get(cogY);
		Double d = (Double) input.get(delta);
		double [] photonChargeArray = (double[]) input.get(photonCharge);
		int [] showerPixelArray = (int[]) input.get(showerPixel);
		
		
		
		return input;
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

	private String cogX;
	private String cogY;
	private String delta;
	private String size;
	private String photonCharge;
	private String showerPixel;
	
	final private double pixelRadius = 1;
	
	static Logger log = LoggerFactory.getLogger(ConcentrationCore.class);
}
