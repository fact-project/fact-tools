/**
 * 
 */
package fact.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.ProcessContext;
import stream.annotations.Description;
import stream.Data;
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

	String key = "Data";

	String file;

	DataOutputStream outputStream;

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
	 * @return the file
	 */
	public String getFile() {
		return file;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(String file) {
		this.file = file;
	}

	/**
	 * @see stream.io.CsvWriter#init(stream.ProcessContext)
	 */
	@Override
	public void init(ProcessContext ctx) throws Exception {
	}

	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data data) {

		try {

			if (outputStream == null) {

				if (file == null) {
					log.error("No output file specified!");
					return null;
				}

				File outFile = new File(file);
				log.debug("Creating new output-stream to '{}'", outFile);
				outputStream = new DataOutputStream(new FileOutputStream(
						outFile));
			}

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
				log.warn("Input for key '{}' is not an array, writing serialized Java object...");
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(value);
				oos.flush();
				oos.close();
				outputStream.write(baos.toByteArray());
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
		outputStream.close();
	}
}