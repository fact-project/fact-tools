package fact.features;

import fact.Utils;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class Leakage implements Processor {
	static Logger log = LoggerFactory.getLogger(Leakage.class);

    @Parameter(required = true)
	private String pixelSetKey;
    @Parameter(required = true)
	private String weights;
    @Parameter(required = true)
	private String leakage1OutputKey;
    @Parameter(required = true)
	private String leakage2OutputKey;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

	@Override
	public Data process(Data input) {
		Utils.mapContainsKeys( input, pixelSetKey, weights);

		PixelSet showerPixel = (PixelSet) input.get(pixelSetKey);
		double[] photonCharge = (double[]) input.get(weights);
		
		
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

		
		input.put(leakage1OutputKey , leakageBorder);
		input.put(leakage2OutputKey , leakageSecondBorder);
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

	public void setPixelSetKey(String pixelSetKey) {
		this.pixelSetKey = pixelSetKey;
	}

	public String getWeights() {
		return weights;
	}
	public void setWeights(String weights) {
		this.weights = weights;
	}

	public String getLeakage1OutputKey() {
		return leakage1OutputKey;
	}

	public void setLeakage1OutputKey(String leakage1OutputKey) {
		this.leakage1OutputKey = leakage1OutputKey;
	}

	public String getLeakage2OutputKey() {
		return leakage2OutputKey;
	}

	public void setLeakage2OutputKey(String leakage2OutputKey) {
		this.leakage2OutputKey = leakage2OutputKey;
	}

	

}
