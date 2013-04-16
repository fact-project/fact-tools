/**
 * 
 */
package fact.image;

import stream.Processor;
import stream.Data;

/**
 * @author chris
 * 
 */
public class SliceSmoothing implements Processor {

	String[] keys = new String[] { "Data" };

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		float[] data = (float[]) input.get("Data");
		float[] out = new float[data.length];

		int roi = data.length / 1440;
		for (int pixel = 0; pixel < 1440; pixel++) {
			out[pixel * roi] = data[pixel * roi];
			out[pixel * roi + roi - 1] = data[pixel * roi + roi - 1];

			for (int s = 1; s < roi - 1; s++) {
				float last = out[pixel * roi + s - 1];
				float next = data[pixel * roi + s + 1];
				out[pixel * roi + s] = (last + next) / 2.0f;
			}
		}

		input.put("DataSmoothed", out);
		return input;
	}
}
