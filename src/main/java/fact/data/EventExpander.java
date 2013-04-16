/**
 * 
 */
package fact.data;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import stream.Data;
import stream.data.DataFactory;
import fact.viewer.ui.DefaultPixelMapping;

/**
 * This class provides the functionality to convert a single FACT event to a
 * series (List) of data items, each item holding the slices of a single pixel.
 * 
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 * 
 */
public class EventExpander {

	final static DefaultPixelMapping map = new DefaultPixelMapping();

	public static List<Data> expand(Data event, int numberOfPixels,
			String dataKey, int fromSlice, int toSlice) {

		List<Data> pixels = new ArrayList<Data>(numberOfPixels);

		Serializable value = event.get(dataKey);
		if (value == null) {
			return pixels;
		}

		if (!value.getClass().isArray()) {
			throw new RuntimeException("Object for key '" + dataKey
					+ "' is not an array!");
		}

		/*
		 * float[] data = (float[]) event.get( dataKey ); if( data == null )
		 * return pixels;
		 */

		Integer eventId = new Integer("" + event.get(FactEvent.EVENT_ID_KEY));
		Integer triggerType = new Integer(""
				+ event.get(FactEvent.TRIGGER_TYPE_KEY));

		int roi = Array.getLength(value) / numberOfPixels;
		DecimalFormat df = new DecimalFormat(dataKey + "_000");

		for (int i = 0; i < numberOfPixels; i++) {
			Data pixel = DataFactory.create();
			pixel.put("@pixel", "" + i);
			pixel.put("@chid", "" + i);
			pixel.put("@EventID", eventId);
			pixel.put("@TriggerType", triggerType);
			pixel.put("@SoftID", map.getSoftwareID(i));
			pixel.put("@gridX", map.getGeomX(i));
			pixel.put("@gridY", map.getGeomY(i));
			pixel.put("x", (20.0d + map.getGeomX(i)) / 20.0d);
			pixel.put("y", (20.0d + map.getGeomY(i)) / 20.0d);

			for (int j = 0; j < roi; j++) {
				if (j >= fromSlice && j <= toSlice) {
					String key = df.format(j);
					String val = Array.get(value, i * roi + j) + "";
					try {
						Double d = new Double(val);
						pixel.put(key, d);
					} catch (Exception e) {
						pixel.put(key, val);
					}
				}
			}

			pixels.add(pixel);
		}

		return pixels;
	}
}
