/**
 * 
 */
package fact.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.annotations.Description;
import fact.utils.SimpleFactEventProcessor;

/**
 * Normalizes all values in a pixel. That means only  0 < value < 1 are should be output.  
 * @author Kai
 * 
 */
@Description(group = "Data Stream.FACT")
public class SliceNormalization extends SimpleFactEventProcessor<double[], double[]> {

	static Logger log = LoggerFactory.getLogger(SliceNormalization.class);

	
	public double[] processSeries(double[] value){
		log.debug("Normalizing image array (key: {})...");

		double[] image =  value;
		double[] normalizedSlices =  new double[image.length];
		
		
		int pixels = 1440;
		int roi = image.length / pixels;

		
		for (int pix = 0; pix < pixels; pix++) {
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			for (int slice = 0; slice < roi; slice++) {
				int pos = pix * roi + slice;
				min = Math.min(min, image[pos]);
				max = Math.max(max, image[pos]);
			}

			double range = Math.abs(max) - Math.abs(min);
			
			for (int slice = 0; slice < roi; slice++) {
				int pos = pix * roi + slice;
				normalizedSlices[pos] = (Math.abs(min) + image[pos]) / (2 * range);
			}
		}
		return normalizedSlices;
	}
}
