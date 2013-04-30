/**
 * 
 */
package fact.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Processor;
import stream.annotations.Description;
import stream.annotations.Parameter;
import stream.Data;

/**
 * @author chris
 * 
 */
@Description(group = "Data Stream.FACT")
public class SliceQuantileDiscretization implements Processor {

	static Logger log = LoggerFactory
			.getLogger(SliceQuantileDiscretization.class);
	String key = "Data";
	Integer bins = 10;

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	@Parameter(required = true, description = "The keys/attributes holding the FACT image array")
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the bins
	 */
	public Integer getBins() {
		return bins;
	}

	/**
	 * @param bins
	 *            the bins to set
	 */
	@Parameter(required = true, description = "Sets the number of bins (quantiles) used for discretization", min = 1.0)
	public void setBins(Integer bins) {
		this.bins = bins;
	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {
		float[] image = (float[]) input.get(getKey());
		float[] output = new float[image.length];
		int roi = image.length / 1440;
		log.info("region-of-interest is: {}", roi);

		long start = System.currentTimeMillis();
		for (int slice = 0; slice < roi; slice++) {
			Quantiles quantiles = new Quantiles(slice, image);
			// log.info("quantiles: {}", quantiles);

			Double step = 1.0 / bins.doubleValue();
			Double[] phis = new Double[bins];
			for (int i = 0; i < phis.length; i++) {
				phis[i] = (i + 1) * step;
			}

			double[] ranges = new double[bins];

			for (int i = 0; i < phis.length; i++) {
				float phi = phis[i].floatValue();
				float quantile = quantiles.getQuantile(phi);
				ranges[i] = quantile;
				// log.info("{}-quantile is {}", fmt.format(phi), quantile);
			}

			for (int pixel = 0; pixel < 1440; pixel++) {

				float value = image[pixel * roi + slice];
				int j = 0;
				while (j < ranges.length && value > ranges[j])
					j++;

				output[pixel * roi + slice] = (float) ranges[j];
			}
			// quantiles.print(phis);
		}
		input.put("Data:QuantileDiscretization", output);
		long end = System.currentTimeMillis();
		log.info("Completed quantile discretization in {} ms", end - start);
		// CommandLine.waitForReturn();
		return input;
	}
}
