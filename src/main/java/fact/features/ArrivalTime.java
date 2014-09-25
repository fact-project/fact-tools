/**
 * 
 */
package fact.features;

import fact.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;

/**
 * Finds pulse arrival time by searching the 25 slices prior to the maximum and taking the time slice where the amplitude is equal to or just larger than 1/2 the max. 
 *  * Input and output are both arrays of size NUMBEROFPIXEL with lists of positions for each pixel.
 * 
 *@author Katie Gray &lt;kathryn.gray@tu-dortmund.de&gt;
 * 
 */
public class ArrivalTime implements Processor {
	static Logger log = LoggerFactory.getLogger(ArrivalTime.class);

    @Parameter(required = true)
    private String key;
    @Parameter(required = true)
    private String outputKey;
    	//positions of arrival times
    @Parameter(required = true)
    private String maxAmpPositionKey;
    	//positions of max pulse amplitude
    @Parameter(required = false)
    private String visualizeKey;


	@Override
	public Data process(Data input) {
        double[] data = (double[]) input.get(key);
		ArrayList[] maxAmpPositions = (ArrayList[]) input.get(maxAmpPositionKey);
        int roi = data.length / Constants.NUMBEROFPIXEL;
        ArrayList[] arrivalTimes =  new ArrayList[Constants.NUMBEROFPIXEL];
        double[] visualizePositions = new double[data.length];
    	//zero for all positions except where an arrival time is found
        
        for(int i = 0; i < data.length; i++){
        	visualizePositions[i] = 0;
        }
        
		//for each pixel
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			arrivalTimes[pix] = findArrivalTimes(pix, roi, data, maxAmpPositions, visualizePositions);
		}
        input.put(outputKey, arrivalTimes);
        input.put(visualizeKey, visualizePositions);
 //   	System.out.println(Arrays.toString(arrivalTimes));


		return input;
	}

    /**
     * @param pix Pixel to check
     * @param roi Basically the number of slices in one event
     * @param data the array which to check
     * @return
     */
	
    public ArrayList findArrivalTimes(int pix, int roi, double[] data, ArrayList[] maxAmpPositions, double[] visualizePositions){
      
		ArrayList<Integer> positions = new ArrayList<Integer>();

        if(!maxAmpPositions[pix].isEmpty()){
        	int numberPulses = maxAmpPositions[pix].size();        	
        	for(int i = 0; i < numberPulses; i++){
                  int Position = 0;
                  int end = (Integer) maxAmpPositions[pix].get(i);
                  int endPos = pix * roi + end;
                  for(int slice = end; slice > end - 25; slice--){
        			   int pos = pix * roi + slice;
        			   if(end - 25 < 0) {continue;}
        	           double value = data[pos];
        	           if(slice > 0 && slice + 80 < roi && end - slice < 15){  
        	        	   if(value <= data[endPos]/2){
        	        		   Position = slice;
        	        		   break;
        	        	   }
        	           }
                  }
                  if(Position != 0) {
                	  positions.add(Position);
                	  visualizePositions[pix*roi+Position] = 15;
                }
        	}
        }

        	return positions;
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

	public String getmaxAmpPositionKey() {
		return maxAmpPositionKey;
	}

	public void setmaxAmpPositionKey(String maxAmpPositionKey) {
		this.maxAmpPositionKey = maxAmpPositionKey;
	}

	public String getVisualizeKey() {
		return visualizeKey;
	}

	public void setVisualizeKey(String visualizeKey) {
		this.visualizeKey = visualizeKey;
	}

}