/**
 * 
 */
package fact.example;

import stream.Data;

/**
 * <p>
 * Example processor implementation for FACT data.
 * </p>
 * 
 * @author Christian Bockermann
 * 
 */
public class SliceSum extends stream.AbstractProcessor {

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data item) {

		int roi = 300;

		float[] result = new float[1440];
		float[] pixels = (float[]) item.get("Data");

		for (int pixel = 0; pixel < 1440; pixel++) {
			result[pixel] = 0;
			for (int s = 0; s < roi; s++) {
				float val = pixels[pixel * roi + s];
				result[pixel] += val;
			}
		}
		item.put("sliceSum", result);

		return item;
	}
}
