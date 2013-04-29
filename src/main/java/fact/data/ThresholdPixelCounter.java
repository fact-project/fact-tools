/**
 * 
 */
package fact.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;

/**
 * This processor simply calculates the maximum value for all time slices in each Pixel.  
 * 
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class ThresholdPixelCounter extends SimpleFactEventProcessor<float[], Long>{
	static Logger log = LoggerFactory.getLogger(MaxAmplitude.class);
	private float maxValue = 2048;
	private long counter = 0;
	

	
	public float getMaxValue() {
		return maxValue;
	}
	public void setMaxValue(float maxValue) {
		this.maxValue = maxValue;
	}
	@Override
	public Long processSeries(float[] data) {
		int roi = data.length / Constants.NUMBEROFPIXEL;
		int pC = 0;
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			// result[pix*roi] =
			// iterate over all slices
			for (int slice = 0; slice < roi; slice++) {
				int pos = pix * roi + slice;
				if(data[pos] > maxValue){
					pC++;
					break;
				}
			}
		}
		counter += pC;
		return counter;
	}
}