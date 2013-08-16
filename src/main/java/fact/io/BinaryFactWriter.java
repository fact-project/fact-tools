/**
 * 
 */
package fact.io;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.annotations.Description;
import stream.io.CsvWriter;

/**
 * <p>
 * This class writes out FACT events in binary format. The format for each event
 * is exactly 1440 * ROI double values. By default the data is expected to be
 * contained in the &quot;Data&quot; property of the input.
 * </p>
 * 
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 * 
 */
@Description(group = "Data Stream.Output", text = "")
public class BinaryFactWriter extends CsvWriter {

	static Logger log = LoggerFactory.getLogger(BinaryFactWriter.class);

	String[] keys = null;

//	String file;

	DataOutputStream outputStream;

	

	/**
	 * @see stream.io.CsvWriter#init(stream.ProcessContext)
	 */
	@Override
	public void init(ProcessContext ctx) throws Exception {
		if (outputStream == null) {

			//File outFile = new File(file);
			log.debug("Creating new output-stream to '{}'", url);
			url = new URL(urlString);

			File outFile = new File(url.getFile());
			log.debug("Creating new output-stream to '{}'", outFile);
			outputStream = new DataOutputStream(new BufferedOutputStream( new FileOutputStream(
					outFile)));
		}
		if(keys==null){
			log.error("No keys provided. This wont work");
		}
	}

	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data data) {
		for (String key: keys){
			try {
				Serializable value = data.get(key);
				if (value.getClass().isArray()) {
	
					for (int i = 0; i < Array.getLength(value); i++) {
						try {
							Float f = new Float(Array.get(value, i) + "");
							outputStream.writeFloat(f.floatValue());
						} catch (Exception e) {
							log.error("Failed to write float: {}", e.getMessage());
						}
					}
				} else {
					log.warn("Input for key '{}' is not an array ...");
				}
	
			} catch(NullPointerException e){
				throw new RuntimeException("Value from map with the key + " + key + " was null: "
						+ e.getMessage());
			} catch (Exception e) {
				throw new RuntimeException("Failed to write file: "
						+ e.getMessage());
			}
		}
		return data;
	}

	/**
	 * @see stream.io.CsvWriter#close()
	 */
	@Override
	public void finish() throws Exception {
		outputStream.close();
	}
	
	
	/**
	 * @return the key
	 */
	public String[] getKeys() {
		return keys;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKeys(String[] keys) {
		this.keys = keys;
	}
	
}