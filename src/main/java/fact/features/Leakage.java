package fact.features;

import fact.Utils;
import fact.mapping.FactCameraPixel;
import fact.mapping.FactPixelMapping;
import fact.viewer.ui.DefaultPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class Leakage implements Processor {
	static Logger log = LoggerFactory.getLogger(Leakage.class);

    @Parameter(required = true)
	private String shower;
    @Parameter(required = true)
	private String weights;
    @Parameter(required = true)
	private String outputKey;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

	@Override
	public Data process(Data input) {
		Utils.mapContainsKeys(getClass(), input, shower, weights);
	
		int[] 	showerPixel = (int[])input.get(shower);
		double[] photonCharge = (double[]) input.get(weights);
		
		
		double size = 0;
	
	    double leakageBorder          = 0;
	    double leakageSecondBorder    = 0;

	    for (int pix: showerPixel)
	    {
	    	size += photonCharge[pix];
	        if (isBorderPixel(pix) )
	        {
	            leakageBorder          += photonCharge[pix];
	            leakageSecondBorder    += photonCharge[pix];
	        }
	        else if (isSecondBorderPixel(pix))
	        {
	            leakageSecondBorder    += photonCharge[pix];
	        }
	    }
	    leakageBorder          = leakageBorder        / size;
	    leakageSecondBorder    = leakageSecondBorder  / size;

		
		input.put(outputKey , leakageBorder);
		input.put(outputKey + "2" , leakageSecondBorder);
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

	
	public String getShower() {
		return shower;
	}
	public void setShower(String shower) {
		this.shower = shower;
	}
	

	public String getWeights() {
		return weights;
	}
	public void setWeights(String weights) {
		this.weights = weights;
	}
	

	public String getOutputKey() {
		return outputKey;
	}
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}
}
