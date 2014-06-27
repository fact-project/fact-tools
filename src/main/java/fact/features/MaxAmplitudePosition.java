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

/**
 * This processor simply calculates the position of the maximum value for all time slices in each Pixel.
 * outputs an int array  
 * 
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class MaxAmplitudePosition implements Processor {
	static Logger log = LoggerFactory.getLogger(MaxAmplitudePosition.class);

    @Parameter(required = true)
    private String key;
    @Parameter(required = true)
    private String outputKey;

	private float minValue= -3000;
	private float maxValue = 3000;
	
	private int searchWindowLeft = 0;
	private int searchWindowRight = 30000;


	@Override
	public Data process(Data input) {
        double[] data = (double[]) input.get(key);
        int roi = data.length / Constants.NUMBEROFPIXEL;

        int[] positions =  new int[Constants.NUMBEROFPIXEL];

		if (searchWindowLeft < 0){
			searchWindowLeft = 0;
		}
		if (searchWindowRight > roi){
			searchWindowRight = roi;
		}
		//foreach pixel
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			positions[pix] = findMaximumPosition(pix, roi, data);
		}
        input.put(outputKey, positions);
		return input;
	}

    /**
     * finds the position of the highest value in the array. If the maximum value is not unique the last position will be used
     * @param pix Pixel to check
     * @param roi Basically the number of slices in one event
     * @param data the array which to check
     * @return
     */
    public int findMaximumPosition(int pix, int roi, double[] data){
        double tempMaxValue = 0;
        int position = 0;
        //iterate over all slices
        for (int slice = searchWindowLeft; slice < searchWindowRight; slice++) {
            int pos = pix * roi + slice;
            //temp save the current value
            double value = data[pos];
            //update maxvalue and position if current value exceeds old value and is still below the threshold set by the user
            if( value > tempMaxValue && value <= maxValue && value >= minValue ){
                tempMaxValue = value;
                position = slice;
            }
        }
        return position;
    }

	/*
	 * Getter and Setter
	 */
	
	public float getMinValue() {
		return minValue;
	}
	public void setMinValue(float minValue) {
		this.minValue = minValue;
	}
	
	public float getMaxValue() {
		return maxValue;
	}
	public void setMaxValue(float maxValue) {
		this.maxValue = maxValue;
	}


	public int getSearchWindowLeft() {
		return searchWindowLeft;
	}


	public void setSearchWindowLeft(int searchWindowLeft) {
		this.searchWindowLeft = searchWindowLeft;
	}


	public int getSearchWindowRight() {
		return searchWindowRight;
	}


	public void setSearchWindowRight(int searchWindowRight) {
		this.searchWindowRight = searchWindowRight;
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
	

}
