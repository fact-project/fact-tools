/**
 * 
 */
package stream.clustering;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import stream.Data;
import stream.data.Statistics;

/**
 * @author chris
 * 
 */
public class ZNormalization {

	public List<Data> normalize(List<Data> items) {

		Set<String> keys = new TreeSet<String>();
		Statistics max = new Statistics();
		Statistics min = new Statistics();
		for (Data item : items) {
			for (String key : item.keySet()) {
				Serializable value = item.get(key);
				if (value instanceof Number) {
					keys.add(key);
					Double val = ((Number) value).doubleValue();
					if (max.get(key) == null || max.get(key) <= val) {
						max.put(key, val);
					}
					if (min.get(key) == null || min.get(key) >= val) {
						min.put(key, val);
					}
				}
			}
		}

		for (Data item : items) {
			for (String key : item.keySet()) {
				Serializable value = item.get(key);
				if (value instanceof Number) {
					Double minimum = min.get(key);
					Double maximum = max.get(key);
					Double range = maximum;
					if (minimum < 0)
						range += Math.abs(minimum);
					Double val = ((Number) value).doubleValue();
					Double norm = val / range;
					if (minimum < 0) {
						norm = (val + Math.abs(minimum)) / range;
					}
					item.put(key, norm);
				}
			}
		}

		return items;
	}
}
