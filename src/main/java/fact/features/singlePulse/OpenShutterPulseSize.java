/**
 * 
 */
package fact.features.singlePulse;

import fact.Constants;
import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;

/**
 * Finds the integral of pulses by defining the specific baseline for each pulse to account for negative signal
 * 
 *@author Katie Gray &lt;kathryn.gray@tu-dortmund.de&gt;
 * 
 */
public class OpenShutterPulseSize implements Processor {
	static Logger log = LoggerFactory.getLogger(OpenShutterPulseSize.class);

    @Parameter(required = true)
    private String key;
    @Parameter(required = true)
    private String outputKey;
    	//size of pulse
    @Parameter(required = true)
    private String arrivalTimeKey;
    	//positions of arrival times - slice where the integral begins
    @Parameter(required = false)
    private String baselineKey;
    	//values that determine the baseline for that pulse, ie at beginning of rising edge, arrival time, etc
    @Parameter(required = true)
    private int width;
    	//number of slices over which we integrate

	@Override
	public Data process(Data input) {
        double[] data = (double[]) input.get(key);
        int roi = data.length / Constants.NUMBEROFPIXEL;
		int[][] arrivalTimes = (int[][]) input.get(arrivalTimeKey);
		double[][] baselineValues = (double[][]) input.get(baselineKey);
	    double[][] pulseSizes = new double[Constants.NUMBEROFPIXEL][];
      
		//for each pixel
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			pulseSizes[pix] = new double[arrivalTimes[pix].length];
			pulseSizes[pix] = calculateSizes(pix, roi, data, arrivalTimes, baselineValues);
		}
		
        input.put(outputKey, pulseSizes);
        
		return input;
	}

    /**
     * @param pix Pixel to check
     * @param roi Basically the number of slices in one event
     * @param data the array which to check
     * @return
     */
	
    public double[] calculateSizes(int pix, int roi, double[] data, int[][] arrivalTimes, double[][] baselineValues){
      //changed from int to double
		ArrayList<Double> sizes = new ArrayList<Double>();
    	
        if(arrivalTimes[pix].length > 0){
        	int numberPulses = arrivalTimes[pix].length;
        	
        	//return an empty list for any pixel where the number of pulses is not equal to the number of baseline values
        	if(numberPulses != baselineValues[pix].length){
        		System.out.println("Error - arrival times don't match up with baseline values");
        		return Utils.arrayListToDouble(sizes);
        	}
        	
        	for(int i = 0; i < numberPulses; i++){
                  double integral = 0;
                  int start =  arrivalTimes[pix][i];
                  double baseline =  baselineValues[pix][i];
                  
                
                  //ignore pulses that are too close together
                  if(numberPulses > 1 && i != 0){
                	 int first =  arrivalTimes[pix][i-1];
                	 int second =  arrivalTimes[pix][i];

                	  if(second - first < width){
                		  continue;
                	  }
                  }
                  
                  for(int slice = start; slice < start + width; slice++){
        			   int pos = pix * roi + slice;
        			   integral += (data[pos] - baseline);
                  }
                  if(integral > 0) sizes.add(integral);
        	}		
        }
        return Utils.arrayListToDouble(sizes);
    }   
     
	/*
	 * Getters and Setters
	 */

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

	public String getArrivalTimeKey() {
		return arrivalTimeKey;
	}

	public void setArrivalTimeKey(String arrivalTimeKey) {
		this.arrivalTimeKey = arrivalTimeKey;
	}

	public String getBaselineKey() {
		return baselineKey;
	}

	public void setBaselineKey(String baselineKey) {
		this.baselineKey = baselineKey;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}
    

}