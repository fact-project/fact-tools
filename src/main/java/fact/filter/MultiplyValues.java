package fact.filter;

import fact.Constants;
import fact.utils.SimpleFactEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.annotations.Parameter;

/**
 * This operator simply multiplies all values by the given factor.
 * 
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class MultiplyValues extends SimpleFactEventProcessor<double[], double[]> {
	static Logger log = LoggerFactory.getLogger(MultiplyValues.class);
	
	private double factor = -1;


	@Override
	public double[] processSeries(double[] data) {

		double[] arr = new double[data.length];
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
	public double getFactor() {
		return factor;
	}
	@Parameter(required = false, description = "The factor by which to multiply", defaultValue="-1")
	public void setFactor(double threshold) {
		this.factor = threshold;
	}
}
