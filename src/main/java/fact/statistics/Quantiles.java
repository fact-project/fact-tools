/**
 * 
 */
package fact.statistics;

import java.text.DecimalFormat;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris
 * 
 */
public class Quantiles {

	static Logger log = LoggerFactory.getLogger(Quantiles.class);

	final float[] values = new float[1440];

	public Quantiles(int slice, float[] image) {

		int roi = image.length / 1440;
		// log.info("ROI is {}", roi);

		for (int pixel = 0; pixel < 1440; pixel++) {
			float value = image[pixel * roi + slice];
			values[pixel] = value;
		}

		Arrays.sort(values);
		// log.info("Sorted values of slice {}:\n\t{}", slice, values);
	}

	public float getQuantile(double phi) {

		double ph = phi;
		if (ph > 1.0)
			ph = 1.0;

		Double len = values.length * ph;
		log.debug("{}-quantile is at rank {} (len: " + len + ")", ph,
				len.intValue());
		return values[Math.min(values.length - 1, len.intValue())];
	}

	public void print(Double[] phis) {
		DecimalFormat fmt = new DecimalFormat("0.00");

		for (double phi : phis) {
			System.out.println(fmt.format(phi) + " " + getQuantile(phi));
		}
	}
}
