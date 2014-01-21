/**
 * 
 */
package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import fact.utils.SimpleFactEventProcessor;

/**
 * This processor simply calculates the position of the maximum value for all time slices in each Pixel.
 * outputs an int array  
 * 
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class MaxAmplitudePosition extends SimpleFactEventProcessor<double[], int[]> {
	static Logger log = LoggerFactory.getLogger(MaxAmplitudePosition.class);
	private float minValue= -3000;
	private float maxValue = 3000;
	
	private int searchWindowLeft = -10;
	private int searchWindowRight = 30000;


	@Override
	public int[] processSeries(double[] data) {
		int[] positions =  new int[Constants.NUMBEROFPIXEL];
		int roi = data.length / Constants.NUMBEROFPIXEL;
		
		if (searchWindowLeft < 0){
			searchWindowLeft = 0;
		}
		if (searchWindowRight > roi){
			searchWindowRight = roi;
		}
		//foreach pixel
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			//initiate maxValue and postion
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
			positions[pix] = position;  
		}
		return positions;
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
	

}
