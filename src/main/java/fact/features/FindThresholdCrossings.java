package fact.features;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.Constants; 

/**
 * 
 * Calculates the positions of pulse candidates by finding threshold crossings with boundary restrictions. 
 * Outputs an array containing a list of positions for each pixel.
 * 
 * @author Katie Gray &lt;kathryn.gray@tu-dortmund.de&gt;
 * 
 */

public class FindThresholdCrossings implements Processor {
	static Logger log = LoggerFactory.getLogger(PhotonCharge.class);

	@Parameter(required = true)
	private String key;
	@Parameter(required = false)
	private String outputKey;
	@Parameter(required = false)
	private String visualizeOutputKey;
	
	private int minBelow = 0;
	private int minAbove = 0;
	
	private double threshold = 5; 
	
	/*
	 * minBelow is minimum number of slices required to be below the threshold before the threshold crossing
	 * minAbove is minimum number of slices required to be above the threshold after the threshold crossing
	 * threshold is the cutoff value
	 *
	 */
	
	
	@Override
	public Data process(Data input) {

		double[] data = (double[]) input.get(key);
		int roi = data.length / Constants.NUMBEROFPIXEL;
		
		//the graph of the array positions visually shows the positions of the crossings for an individual pixel
	    int[] positions =  new int[data.length];
	    
	    //the array CrossPositions contains lists of positions of crossings for each pixel
	    ArrayList[] CrossPositions = new ArrayList[Constants.NUMBEROFPIXEL];

	    	for(int pix=0; pix < Constants.NUMBEROFPIXEL; pix++){	    		
	    		//creates a list of positions of threshold crossings for a single pixel
	    		ArrayList<Integer> slices = new ArrayList<Integer>();
	    		
	    		for(int slice=0; slice < roi; slice++){
	    			if(candidate(pix, slice, roi, data) == true) {
	    				slices.add(slice);
	    				positions[pix*roi+slice]=5;
	    				slice += minAbove;
	    			}
	    			else {
	    				positions[pix*roi+slice]=0;
	    			}
	    		}
	    		
	    		//	if no crossing, list for that pixel is empty	    		
	    		
	    		CrossPositions[pix] = slices;
	    	}
		 
			input.put(visualizeOutputKey, positions);
			input.put(outputKey, CrossPositions);
			return input;
		
	}
	
	//to determine threshold crossings
	public boolean candidate(int pix, int slice, int roi, double[] data){
		boolean answer = true;

		int pos = pix * roi + slice;
		
		if(data[pos] < threshold){return false;}
	
		for(int i=1; i < minBelow; i++){
			if(slice-i < 0){return false;}
			if(data[pos-i] >= threshold){return false;}
			}

		for(int k=0; k < minAbove; k++){
			if(slice+k > roi){return false;}
			if(pos+k == Constants.NUMBEROFPIXEL * roi) {return false;}    
			if(data[pos+k] <= threshold){return false;}
			}
	
		return answer;
	}

	//Getters and Setters
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public String getVisualizeOutputKey() {
		return visualizeOutputKey;
	}

	public void setVisualizeOutputKey(String visualizeOutputKey) {
		this.visualizeOutputKey = visualizeOutputKey;
	}

	public int getMinBelow() {
		return minBelow;
	}

	public void setMinBelow(int minBelow) {
		this.minBelow = minBelow;
	}

	public int getMinAbove() {
		return minAbove;
	}

	public void setMinAbove(int minAbove) {
		this.minAbove = minAbove;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	
	

}
