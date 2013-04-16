/**
 * 
 */
package fact.image;

import fact.image.overlays.PixelSet;
import stream.Processor;
import stream.Data;

/**
 * @author chris
 * 
 */
public class SelectPixels implements Processor {

	String key = "@pixelset";

	Integer[] ids;

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
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the ids
	 */
	public Integer[] getIds() {
		return ids;
	}

	/**
	 * @param ids
	 *            the ids to set
	 */
	public void setIds(Integer[] ids) {
		this.ids = ids;
	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		if (ids == null || ids.length == 0 || key == null)
			return input;

		PixelSet set = new PixelSet();

		for (Integer id : ids) {
			set.add(new Pixel(id));
		}

		input.put(key, set);

		return input;
	}
}
