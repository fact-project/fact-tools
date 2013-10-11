/**
 * 
 */
package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.utils.SimpleFactPixelProcessor;

/**
 * This processor simply calculates the maximum value for all time slices in each Pixel. 
 * The output is a float array with an entry for each Pixel.
 * 
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class MaxAmplitude extends SimpleFactPixelProcessor{
	static Logger log = LoggerFactory.getLogger(MaxAmplitude.class);
	private float minValue= -3000;
	private float maxValue = 3000;	
	
	@Override
	public double processPixel(double[] pixelData) {
		double tempMaxValue = 0;
		for (int slice = 0; slice < pixelData.length; slice++) {
			double value = pixelData[slice];
			//update maxvalue and position if current value exceeds old value and is still below the threshold set by the user
			if( value > tempMaxValue  && value <= maxValue && value >= minValue ){
				tempMaxValue = value;
			}
		}
		return tempMaxValue;
	}

	// Getter and Setter//
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
