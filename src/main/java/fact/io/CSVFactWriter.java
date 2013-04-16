/**
 * 
 */
package fact.io;

import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.io.CsvWriter;
import fact.data.EventExpander;

/**
 * <p>
 * This class writes out FACT events in CSV format. The format for each event is
 * exactly 1440 * ROI double values. By default the data is expected to be
 * contained in the &quot;Data&quot; property of the input.
 * </p>
 * 
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 * 
 */
public class CSVFactWriter extends CsvWriter {

	static Logger log = LoggerFactory.getLogger(CSVFactWriter.class);

	String key = "Data";

	CsvWriter writer = null;

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
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data data) {

		try {
			
			if (writer == null) {
				
				//if (file == null) {
				//	log.error("No output file specified!");
				//	return null;
				//}

				//File outFile = new File(file);
				log.debug("Creating new output-stream to '{}'", url);
				url = new URL(urlString);
				writer = new CsvWriter(url);
				//writer = new CsvWriter(outFile, separator);
			}

//			float[] dat = (float[]) data.get(key);
//			if (dat == null)
//				return data;

			List<Data> pixels = EventExpander.expand(data, 1440, key, 0, 300);

			for (Data pixel : pixels) {
				log.debug("Writing pixel {}", pixel);
				writer.process(pixel);
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to write file: "
					+ e.getMessage());
		}

		return data;
	}

	/**
	 * @see stream.io.CsvWriter#close()
	 */
	@Override
	public void finish() throws Exception {
		writer.finish();
	}
}