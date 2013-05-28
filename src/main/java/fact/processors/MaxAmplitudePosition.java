/**
 * 
 */
package fact.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;

/**
 * This processor simply calculates the position of the maximum value for all time slices in each Pixel.
 * outputs an int array  
 * 
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class MaxAmplitudePosition extends SimpleFactEventProcessor<float[], int[]> {
	static Logger log = LoggerFactory.getLogger(MaxAmplitudePosition.class);
	private float minValue= -3000;
	private float maxValue = 3000;


	@Override
	public int[] processSeries(float[] data) {
		int[] positions =  new int[Constants.NUMBEROFPIXEL];
		int roi = data.length / Constants.NUMBEROFPIXEL;
		//foreach pixel
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			//initiate maxValue and postion
			float tempMaxValue = 0;
			int position = 0;
			//iterate over all slices
			for (int slice = 0; slice < roi; slice++) {
				int pos = pix * roi + slice;
				//temp save the current value
				float value = data[pos];
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


}
