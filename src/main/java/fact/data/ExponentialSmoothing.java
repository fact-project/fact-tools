package fact.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.annotations.Parameter;
import fact.Constants;


/**
 * Calculates first Order exponential Smoothing
 * Let y be the original Series and s be the smoothed one.
 *  s_0 = y_0
 *  s_i = alpha*y_i + (1-alpha) * s_(i-1)
 *  see http://en.wikipedia.org/wiki/Exponential_smoothing
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 *
 */
public class ExponentialSmoothing extends SimpleFactEventProcessor<float[],float[]> {
	static Logger log = LoggerFactory.getLogger(ExponentialSmoothing.class);
	
	float alpha = 0.5f;
	
	@Override
	public float[] processSeries(float[] data) {
			int roi = data.length / Constants.NUMBEROFPIXEL;
			float[] smoothedData= new float[data.length];
			//foreach pixel
			for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
				//beginn with startvalue 
				smoothedData[pix*roi] = data[pix*roi];  
				for (int slice = 1; slice < roi; slice++) {
					int pos = pix * roi + slice;
					//glaettung
					smoothedData[pos] = alpha*data[pos] + (1-alpha)*smoothedData[pos-1];
				}
			}
		return smoothedData;
	}

	
	/*
	 * Getter and Setter
	 */
	public float getAlpha() {
		return alpha;
	}
	@Parameter (required = false, description = "This value changes the amount of smoothing that will take place. If alpha equals 1 the values remain unchanged.  See http://en.wikipedia.org/wiki/Exponential_smoothing", min = 0.0 , max = 1.0)
	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

}
