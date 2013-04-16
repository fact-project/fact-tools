/**
 * 
 */
package fact.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fact.image.overlays.PixelSet;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import stream.AbstractProcessor;
import stream.ProcessContext;
import stream.Data;
import stream.io.JSONStream;

/**
 * @author chris
 * 
 */
public class AddAnnotations extends AbstractProcessor {

	File file;
	Map<String, List<PixelSet>> annotations = new HashMap<String, List<PixelSet>>();

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * @see stream.AbstractProcessor#init(stream.ProcessContext)
	 */
	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);

		JSONStream stream = new JSONStream(new FileInputStream(file));
		Data item = stream.readNext();
		while (item != null) {

			String id = item.get("@id") + "";
			PixelSet set = new PixelSet();
			JSONArray overlay = (JSONArray) item.get("@shower");
			for (int i = 0; i < overlay.size(); i++) {
				JSONObject obj = (JSONObject) overlay.get(i);
				Integer sid = new Integer(obj.get("softId") + "");
				set.add(new Pixel(sid));
			}

			List<PixelSet> sets = annotations.get(id);
			if (sets == null) {
				sets = new ArrayList<PixelSet>();
				annotations.put(id, sets);
			}
			sets.add(set);

			item = stream.readNext();
		}

		stream.close();
	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {
		Serializable id = input.get("@id");
		if (id == null)
			return input;
		List<PixelSet> sets = annotations.get(id.toString());
		if (sets != null && !sets.isEmpty())
			input.put("@shower", sets.get(0));
		return input;
	}
}