/**
 * 
 */
package fact.image;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.AbstractProcessor;
import stream.Data;

/**
 * @author chris
 * 
 */
public class SliceInterpolation extends AbstractProcessor {

	static Logger log = LoggerFactory.getLogger(SliceInterpolation.class);
	Double deviations = 3.0;

	String[] keys = new String[] { "DataCalibrated" };

	/**
	 * @return the deviations
	 */
	public Double getDeviations() {
		return deviations;
	}

	/**
	 * @param deviations
	 *            the deviations to set
	 */
	public void setDeviations(Double deviations) {
		this.deviations = deviations;
	}

	/**
	 * @return the keys
	 */
	public String[] getKeys() {
		return keys;
	}

	/**
	 * @param keys
	 *            the keys to set
	 */
	public void setKeys(String[] keys) {
		this.keys = keys;
	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		for (String key : keys) {

			Serializable value = input.get(key);

			float[] image = (float[]) value;

			int pixels = 1440;
			float[] sums = new float[pixels];
			float[] sqSum = new float[pixels];
			int roi = image.length / pixels;

			for (int p = 0; p < pixels; p++) {

				sums[p] = 0.0f;
				sqSum[p] = 0.0f;

				float min = image[p * roi];
				float max = min;

				for (int s = 0; s < roi; s++) {
					float cur = image[p * roi + s];
					if (min > cur)
						min = cur;
					if (max < cur)
						max = cur;

					sums[p] += cur;
					sqSum[p] += (cur * cur);
				}

				float mean = sums[p] / Float.valueOf("" + roi);
				float meanOfSquares = sqSum[p] / roi;

				float var = meanOfSquares - (mean * mean);
				log.info("pixel {}:  mean: {}, variance: " + var, p, mean);

				for (int s = 1; s + 1 < roi; s++) {

					int pos = p * roi + s;

					float diffLast = Math.abs(image[pos - 1] - image[pos]);
					float diffNext = Math.abs(image[pos] - image[pos + 1]);

					if (p == 539 && s > 135 && s < 139) {
						log.info("Pixel {}, Slice {}", p, s);
						log.info("   diffLast: {}", diffLast);
						log.info("   diffNext: {}", diffNext);
					}

					if (diffLast > deviations * var
							|| diffNext > deviations * var) {

						image[pos] = 0.5f * (image[pos - 1] + image[pos + 1]);
					}
				}
			}
		}
		return input;
	}
}