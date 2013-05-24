/**
 * 
 */
package fact.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.AbstractProcessor;
import stream.Data;
import stream.ProcessContext;

/**
 * @author chris
 * 
 */
public class SimpleFileWriter extends AbstractProcessor {

	static Logger log = LoggerFactory.getLogger(SimpleFileWriter.class);
	PrintStream outputStream;
	File file;

	/**
	 * @see stream.AbstractProcessor#init(stream.ProcessContext)
	 */
	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);

		if (file == null) {
			throw new Exception("No 'file' parameter specified!");
		}

		outputStream = new PrintStream(new FileOutputStream(file));
	}

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		try {
			//
			// TODO: Add the code for writing the required attributes
			// to the outputStream.
			//

			// Example: obtain the float[] array and write it as
			// comma-separated list of values:

			float[] values = (float[]) input.get("Data");
			for (int i = 0; i < values.length; i++) {
				outputStream.print(values[i]);
				if (i + 1 < values.length) {
					outputStream.print(",");
				}
			}

			// write a newline after we wrote the item
			//
			outputStream.println();

		} catch (Exception e) {
			log.error("Failed to write item: {}", e.getMessage());
			if (log.isDebugEnabled()) {
				e.printStackTrace();
			}
		}
		return input;
	}

	/**
	 * @see stream.AbstractProcessor#finish()
	 */
	@Override
	public void finish() throws Exception {
		super.finish();

		//
		// upon finishing this processor (ie. as the parent
		// process ends), we close the output stream
		//
		outputStream.close();
	}

	/**
	 * @return the output
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param output
	 *            the output to set
	 */
	public void setFile(File output) {
		this.file = output;
	}
}
