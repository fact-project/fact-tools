package fact.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;

import stream.annotations.Parameter;

/**
 * This operator simply multiplies all values by the given factor.
 * 
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class MultiplyValues extends SimpleFactEventProcessor<float[], float[]> {
	static Logger log = LoggerFactory.getLogger(MultiplyValues.class);
	
	private float factor = -1;


	@Override
	public float[] processSeries(float[] data) {

		float[] arr = new float[data.length];
		int roi = data.length / Constants.NUMBEROFPIXEL;
		//for each pixel
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			//iterate over all slices
			for (int slice = 0; slice < roi; slice++) {
				int pos = pix * roi + slice;
					arr[pos] = data[pos] * factor;
			}
		}
		return arr;	
	}
	

	
	/*
	 * Getter and setter
	 */
	public float getFactor() {
		return factor;
	}
	@Parameter(required = false, description = "The factor by which to multiply", defaultValue="-1")
	public void setFactor(float threshold) {
		this.factor = threshold;
	}
}
