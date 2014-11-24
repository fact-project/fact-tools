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
 * Finds sum of slice amplitudes starting at pulse arrival time 
 * 
 *@author Katie Gray &lt;kathryn.gray@tu-dortmund.de&gt;
 * 
 */
public class PulseSizeCalculator implements Processor {
	static Logger log = LoggerFactory.getLogger(PulseSizeCalculator.class);

    @Parameter(required = true)
    private String key;
    @Parameter(required = true)
    private String outputKey;
    	//size of pulse
    @Parameter(required = true)
    private String arrivalTimeKey;
    	//positions of arrival times 
    @Parameter(required = true)
    private int width;
    	//number of slices over which we integrate

	@Override
	public Data process(Data input) {
        Utils.mapContainsKeys(input, key, arrivalTimeKey);

        double[] data = (double[]) input.get(key);
        int roi = data.length / Constants.NUMBEROFPIXEL;
		ArrayList[] arrivalTimes = (ArrayList[]) input.get(arrivalTimeKey);
	    ArrayList[] pulseSizes = new ArrayList[Constants.NUMBEROFPIXEL];
      
	    //additional output that is a list of values for all events for a single pixel. Currently pixel 0. 
	    ArrayList<Integer> singlePixelPulses = new ArrayList<Integer>();
        Integer singlePixel = 0;
        
		//for each pixel
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			
			pulseSizes[pix] = calculateSizes(pix, roi, data, arrivalTimes);

			
			//creates the list for a single pixel
			if(pix==0){
				if(pulseSizes[pix].size() != 0){
					for(int i = 0; i < pulseSizes[pix].size(); i++){
						singlePixel = (Integer) pulseSizes[pix].get(i);
						singlePixelPulses.add(singlePixel);
					}
				}
			}
		
		}
		input.put("singlePixelPulses", singlePixelPulses);
        input.put(outputKey, pulseSizes);
        
        
//       System.out.println(Arrays.toString(pulseSizes));
//       System.out.println(singlePixelPulses);

		return input;
	}

    /**
     * @param pix Pixel to check
     * @param roi Basically the number of slices in one event
     * @param data the array which to check
     * @return
     */
	
    public ArrayList calculateSizes(int pix, int roi, double[] data, ArrayList[] arrivalTimes){
      
		ArrayList<Integer> sizes = new ArrayList<Integer>();
    	
        if(!arrivalTimes[pix].isEmpty()){
        	int numberPulses = arrivalTimes[pix].size();        	
        	for(int i = 0; i < numberPulses; i++){
                  int integral = 0;
                  int start = (Integer) arrivalTimes[pix].get(i);
                  for(int slice = start; slice < start + width; slice++){
        			   int pos = pix * roi + slice;
        			   integral += data[pos];
                  }
                  sizes.add(integral);
        	}		
        }
        return sizes;
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

	public String getarrivalTimeKey() {
		return arrivalTimeKey;
	}

	public void setarrivalTimeKey(String arrivalTimeKey) {
		this.arrivalTimeKey = arrivalTimeKey;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

}