/**
 * 
 */
package fact.features;

import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Finds pulse arrival time by finding the maximum slope in the leading edges of pulses or by finding the beginning of the pulse.
 * Also can find values to use for a baseline for calculating individual pulse sizes
 * 
 *@author Katie Gray &lt;kathryn.gray@tu-dortmund.de&gt;
 * 
 */
public class ArrivalTimeFromSlope implements Processor {
	static Logger log = LoggerFactory.getLogger(ArrivalTimeFromSlope.class);

    @Parameter(required = true)
    private String key;
    @Parameter(required = true)
    private String derivationKey;
    	//slopes at each time slice; used features.Derivation
    @Parameter(required = true)
    private String outputKey;
    	//positions of arrival times
    @Parameter(required = false)
    private String visualizeKey;
    	//array of size data.length with values of zero except for time slices of arrival times. 
    @Parameter(required = false)
    private String baselineKey;
    	//used in OpenShutterPulseSize to account for negative values
 
    private int skipFirstSlices = 0;
    	//start searching after this number of slices
    private int skipLastSlices = 0;   
    	//stop searching this many slices before the end of the timeline 
    private int width = 1;
    	//should be an odd number. 


	@Override
	public Data process(Data input) {

		if(width%2 == 0){
			width++;
			log.info("ArrivalTimeFromSlope only supports odd window lengths. New length is: " + width);
		}
		
        double[] data = (double[]) input.get(key);
        double[] slopes = (double[]) input.get(derivationKey);
        int roi = data.length / Constants.NUMBEROFPIXEL;
        
        ArrayList[] pulsePeaks =  new ArrayList[Constants.NUMBEROFPIXEL];
        	//the position where pulse leading edges end 
        ArrayList[] arrivalTimes = new ArrayList[Constants.NUMBEROFPIXEL];
        	//arrival times for all pulses in each pixel
        ArrayList[] baselineValues = new ArrayList[Constants.NUMBEROFPIXEL];
        	//value at the slice where you want to set your baseline
        int[] visualizePositions = new int[data.length];
        	//zero for all positions except where an arrival time is found
              
        for(int i = 0; i < data.length; i++){
        	visualizePositions[i] = 0;
        }
                
		//for each pixel
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {			
			pulsePeaks[pix] = findPulsePeaks(pix, roi, slopes);
			arrivalTimes[pix] = findArrivalTimes(pix, roi, width, data, slopes, pulsePeaks, visualizePositions, baselineValues);
		}
		
        input.put(outputKey, arrivalTimes);
        input.put(visualizeKey, visualizePositions);
        input.put(visualizePulsePeakskey);
        input.put(baselineKey, baselineValues);
//        System.out.println(Arrays.toString(baselineValues));
		

		return input;
	}

    /**
     * @param pix - Pixel to check
     * @param roi - the number of slices in one event
     */
	
 //the function that finds the pulses. returns positions of the peaks
    public ArrayList findPulsePeaks(int pix, int roi, double[] slopes){
	
    	ArrayList<Integer> peaks = new ArrayList<Integer>();
		int risingEdgeLength = 10;

		
		for(int slice=0+skipFirstSlices; slice < roi-skipLastSlices; slice++){
			int pos = pix*roi+slice;
			boolean peak = true;
			boolean check = false;
				//allows one slice to have a negative slope in the leading edge
			

			
			if(slopes[pos] <= 0){
				peak = false;
				continue;
			}
			
			else{
				for(int i=0; i < risingEdgeLength; i++){
					if(slice-i >= 0 && slice + i < roi){
						if(slopes[pos-i] < 0){
							if(check == true){
								peak = false;
								break;
							}
							else check = true;

						}
						else{
							continue;
						}
					}
					else{
						peak = false;
						break;
					}
				}
			}											
				
			if(peak == false) {continue;}
				
				int k=0;				
				while(slice+k < roi){
					if(slopes[pos+k] > 0){
						k++;
					}

					else break;
				}
				slice += k - 1;
					
			if(peak == true){
				peaks.add(slice);
			}
		}
		
    	return peaks;
    }
          
	
//the function that finds the starting point of the pulse, defined by the first position with a positive slope, and
//the position of maximum slope. both values can be used for arrival time or baseline values   
	public ArrayList findArrivalTimes(int pix, int roi, int width, double[] data, double[] slopes, ArrayList[] pulsePeaks, int[] visualizePositions, ArrayList[] baselineValues){
		ArrayList<Integer> times = new ArrayList<Integer>();
		ArrayList<Double> baseValues = new ArrayList<Double>();
		ArrayList<Integer> peaks = pulsePeaks[pix];
		int number = peaks.size();
		int pivot = (int) (width/2.0);
		
		for(int pulse = 0; pulse < number; pulse++){
			int end = (Integer) peaks.get(pulse);
		
			//find the starting point of the leading edge
			int current = end;
			while(slopes[pix*roi+current-1] > 0){
				current --;
			}				
			int start = current;		//start is the first position of the leading edge
			
//			accounting for 'false positives':
			if(pix*roi+end < data.length && pix*roi+start > 0){
				double difference = data[pix*roi+end] - data[pix*roi+start];			
				if(difference < 7){
					continue;
				}
			}
	
	//find max slope over leading edge
			int maxpos = 0;
			double maxslope = 0;
			for(int slice = start; slice < end; slice++){
				int pos = pix*roi+slice;

				if(width == 1){
					double currentslope = slopes[pos];
					if(currentslope > maxslope){
						maxslope = currentslope; 
						maxpos = slice;
					}
				}
				
				else{
					if(slice+pivot < end && slice-pivot > start){
						double currentslope = data[pos+pivot] - data[pos-pivot];
						if(currentslope > maxslope){
							maxslope = currentslope;
							maxpos = slice;
						}
					}
				}
			} 
			
			if(start > 0+skipFirstSlices && end < roi-skipLastSlices && end - maxpos < 14){
				visualizePositions[pix*roi+start] = 15;
				times.add(start);
				baseValues.add(data[pix*roi+start]);
			}
				//to use maximum slope instead of first position of rising edge, simply replace start with maxpos. 
		}
		baselineValues[pix] = baseValues;
        return times;
    }
    

     
	/*
	 * Getters and Setters
	 */


	public String getDerivationKey() {
		return derivationKey;
	}

	public void setDerivationKey(String derivationKey) {
		this.derivationKey = derivationKey;
	}

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

	public String getVisualizeKey() {
		return visualizeKey;
	}

	public void setVisualizeKey(String visualizeKey) {
		this.visualizeKey = visualizeKey;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}
	
	public String getBaselineKey() {
		return baselineKey;
	}

	public void setBaselineKey(String baselineKey) {
		this.baselineKey = baselineKey;
	}

	public int getSkipFirstSlices() {
		return skipFirstSlices;
	}

	public void setSkipFirstSlices(int skipFirstSlices) {
		this.skipFirstSlices = skipFirstSlices;
	}

	public int getSkipLastSlices() {
		return skipLastSlices;
	}

	public void setSkipLastSlices(int skipLastSlices) {
		this.skipLastSlices = skipLastSlices;
	}

	
}
