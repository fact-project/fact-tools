package fact.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import fact.utils.SimpleFactEventProcessor;

/**
 * This Processor calculates the Standarddeviation of the slices in each pixel. It uses the Average Processor to calculate the average value ina pixel. 
 * 
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public abstract class StdDeviation extends SimpleFactEventProcessor<float[], float[]> {
	static Logger log = LoggerFactory.getLogger(StdDeviation.class);
	

	public float[] processSeries(float[] data) {
		//get the average value in each pixel
		float[] avgs = new PixelAverage().processSeries(data);
		
		float[] stds = new float[avgs.length];
		
		int roi = data.length / Constants.NUMBEROFPIXEL;
		double difference = 0.0f;
		//foreach pixel
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			//iterate over all slices
			for (int slice = 1; slice < roi; slice++) {
				int pos = pix * roi + slice;
				difference += Math.pow((data[pos]- avgs[pix]),2);
			}
			stds[pix] = (float) Math.sqrt((1/((double)data.length - 1)) * difference); 
			difference = 0.0f;
		}
		return stds;
	}
}


