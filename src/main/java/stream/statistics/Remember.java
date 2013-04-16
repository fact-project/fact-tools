/**
 * 
 */
package stream.statistics;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import stream.AbstractProcessor;
import stream.Data;

/**
 * @author chris
 * 
 */
public class Remember extends AbstractProcessor {

	String[] keys;

	final Map<String, Serializable> values = new LinkedHashMap<String, Serializable>();

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

		if (keys == null || keys.length == 0)
			return input;

		for (String key : keys) {
			if (input.containsKey(key)) {
				values.put(key, input.get(key));
			} else {
				if (values.containsKey(key)) {
					input.put(key, values.get(key));
				}
			}
		}

		return input;
	}
}