package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import fact.EventUtils;
import fact.viewer.ui.DefaultPixelMapping;

public class Leakage implements Processor {
	static Logger log = LoggerFactory.getLogger(Leakage.class);

	private String shower = null;
	private String weights = null;
	private String outputKey = "HillasConcentration";

	@Override
	public Data process(Data input) {
		EventUtils.mapContainsKeys(getClass(), input, shower, weights);
	
		int[] 	showerPixel = (int[])input.get(shower);
		double[] photonCharge = (double[]) input.get(weights);
		
		
		double size = 0;
		for(double s: photonCharge) {size += s;} 
	
	    double leakageBorder          = 0;
	    double leakageSecondBorder    = 0;

	    for (int pix: showerPixel)
	    {
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
		for(int nPix: DefaultPixelMapping.getNeighborsFromChid(pix))
		{
			if(isBorderPixel(nPix)){
				return true;
			}
		}
		
		return false;
	}
	boolean isBorderPixel(int pix){
		for(int i : DefaultPixelMapping.getNeighborsFromChid(pix)){
			if(i == -1) return true;
		}
		return false;
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
