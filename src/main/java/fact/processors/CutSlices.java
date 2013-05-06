/**
 * 
 */
package fact.processors;

import java.io.Serializable;
import java.lang.reflect.Array;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.data.DataFactory;

/**
 * @author chris, niklaswulf
 * 
 */
public class CutSlices implements Processor {
	static Logger log = LoggerFactory.getLogger(CutSlices.class);

	Integer start = 0;
	Integer end = 300;
	Integer elements = 1440;

	String[] keys = new String[] { "Data" };

	/**
	 * @return the start
	 */
	public Integer getStart() {
		return start;
	}

	/**
	 * @param start
	 *            the start to set
	 */
	@Parameter(name = "start", defaultValue = "0", required = false)
	public void setStart(Integer start) {
		this.start = start;
	}

	/**
	 * @return the end
	 */
	public Integer getEnd() {
		return end;
	}

	/**
	 * @param end
	 *            the end to set
	 */
	@Parameter(name = "end", defaultValue = "300", required = false)
	public void setEnd(Integer end) {
		this.end = Math.max(start + 1, end);
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
	@Parameter(name = "keys", defaultValue = "Data", values = { "Data" }, required = false)
	public void setKeys(String[] keys) {
		this.keys = keys;
	}

	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data data) {

		for (String key : keys) {
			if (data.containsKey(key) && data.get(key).getClass().isArray()) {
				Serializable s = data.get(key);
				log.debug("Cutting array of type {}", s.getClass()
						.getComponentType());

				if (s.getClass().isArray()) {

					int rows = elements;
					int oldLen = Array.getLength(s) / rows;

					int newLen = (end - start);

					// Object result = Array.newInstance(s.getClass()
					// .getComponentType(), rows * newLen);

					float[] original = (float[]) s;
					float[] result = new float[newLen * rows];

					for (int row = 0; row < rows; row++) {

						System.arraycopy(original, row * oldLen + start,
								result, row * newLen, newLen);

						// for (int i = 0; i < newLen && i < (end - start); i++)
						// {
						// int fromPos = row * oldLen + start + i;
						// int toPos = row * newLen + i;
						//
						// Array.set(result, toPos, Array.get(s, fromPos));
						// }
					}

					data.put(key, result);

				} else {
					log.error("Skipping non-array type object '{}'", key);
				}

				// float[] dat = (float[]) data.get( key );
				// dat = subArray( dat, dat.length / elements, start, end );
				// data.put( key, dat );
			} else {
				log.warn("Key '{}' is not refering to an array!", key);
			}
		}

		return data;
	}

	protected float[] subArray(float[] data, int rowLength, int start, int end) {

		int oldLen = rowLength;
		int rows = data.length / rowLength;
		log.debug("Old ROI is {}", oldLen);
		int newLen = end - start;

		float[] result = new float[rows * newLen];

		for (int row = 0; row < rows; row++) {

			int off = row * oldLen;
			int newOff = row * newLen;

			for (int i = 0; i < newLen && i < oldLen; i++) {
				result[newOff + i] = data[off + start + i];
			}
		}
		return result;
	}

	public static void main(String[] args) {
		float[] input = new float[] { 1.0f, 1.0f, 2.0f, 2.0f, 1.0f, 1.0f, 2.0f,
				2.0f, 3.0f, 3.0f, 2.0f, 2.0f, 3.0f, 3.0f, 4.0f, 4.0f, 3.0f,
				3.0f };

		int roi = input.length / 3;

		for (int i = 0; i < input.length; i++) {
			System.out.print(" " + input[i] + ", ");
			if (i > 0 && (i + 1) % (roi) == 0) {
				System.out.println();
			}
		}

		CutSlices cut = new CutSlices();
		cut.setStart(2);
		cut.setEnd(4);
		cut.elements = 3;
		cut.setKeys(new String[] { "Data" });

		Data item = DataFactory.create();
		item.put("Data", input);

		item = cut.process(item);

		float[] outcome = (float[]) item.get("Data"); // cut.subArray( input,
														// roi, 2, 4 );
		roi = outcome.length / 3;

		for (int i = 0; i < outcome.length; i++) {
			System.out.print(" " + outcome[i] + ", ");
			if (i > 0 && (i + 1) % roi == 0) {
				System.out.println();
			}
		}
	}
}