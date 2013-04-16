/**
 * 
 */
package fact.image;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import stream.AbstractProcessor;
import stream.Data;
import fact.image.overlays.PixelSet;

/**
 * @author chris
 * 
 */
public class DetectCorePixel extends AbstractProcessor {

	String[] keys = new String[] { "Data" };

	String name = "@core";

	int k = 5;

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
	 * @return the k
	 */
	public int getK() {
		return k;
	}

	/**
	 * @param k
	 *            the k to set
	 */
	public void setK(int k) {
		this.k = k;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		for (String key : keys) {

			Serializable value = input.get(key);
			if (value != null && value.getClass().isArray()
					&& value.getClass().getComponentType() == float.class) {

				float[] image = (float[]) value;
				List<PixelInfo> infos = new ArrayList<PixelInfo>();

				int pixels = 1440;
				float[] sums = new float[pixels];
				int roi = image.length / pixels;

				for (int p = 0; p < pixels; p++) {

					sums[p] = 0.0f;

					float min = image[p * roi];
					float max = min;

					int zeroX = 0;
					float last = Math.signum(min);

					for (int s = 0; s < roi && s < 130; s++) {
						float cur = image[p * roi + s];

						float sign = Math.signum(cur);
						if (last * sign < 0)
							zeroX++;

						last = sign;

						if (130 < s && s < 150)
							continue;

						if (min > cur)
							min = cur;
						if (max < cur)
							max = cur;

						sums[p] += cur;
					}

					float mean = roi * sums[p];
					float var = 0.0f;
					for (int s = 0; s < roi; s++) {
						float sm = (image[p * roi + s] - mean);
						var += (sm * sm);
					}
					var = var / roi;

					if (zeroX < 3)
						infos.add(new PixelInfo(p,
								new Double(Math.abs(0 - min))));

				}

				Collections.sort(infos);
				PixelSet corePixel = new PixelSet();
				Iterator<PixelInfo> it = infos.iterator();
				while (it.hasNext() && corePixel.size() < k) {
					corePixel.add(new Pixel(it.next().id));
				}

				if (name != null)
					input.put(name, corePixel);
				else
					input.put("@core:" + key, corePixel);
			}
		}

		return input;
	}

	class PixelInfo implements Comparable<PixelInfo> {
		final Integer id;
		final Double weight;

		public PixelInfo(Integer id, Double weight) {
			this.id = id;
			this.weight = weight;
		}

		/**
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(PixelInfo arg0) {

			if (arg0 == null)
				return -1;

			int ret = weight.compareTo((arg0).weight);
			if (ret == 0)
				return id.compareTo(id);
			return -ret;
		}
	}
}