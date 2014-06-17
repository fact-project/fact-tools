/**
 * 
 */
package fact.io;


import java.io.PrintStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.annotations.Description;
import stream.annotations.Parameter;
import stream.io.CsvWriter;

/**
 * <p>
 * Adapted from Dominik's writer - outputs a file containing a nested list
 * </p>
 * 
 * @author Dominik Neise &lt;dominik.neise@udo.edu&gt;
 * @editor Katie Gray
 * 
 */
@Description(group = "Data Stream.Output", text = "")
public class CreateNestedListFile extends CsvWriter {
	static Logger log = LoggerFactory.getLogger(CreateNestedListFile.class);
	PrintStream outputStream;
	
	@Parameter(required=true, description="ArrayList<Double>[] sizes of pulses for all events for a single pixel", defaultValue="sizes")
	private String PixPulseSizeKey;
    ArrayList<Integer>[] PixPulseSize = null;
    
    @Parameter(required=true)
    private String urlString;

    
	@Override
	public void init(ProcessContext ctx) throws Exception {
		if (outputStream == null) {
			log.debug("Creating new output-stream to '{}'", url);
			url = new URL(urlString);

			File outFile = new File(url.getFile());
			log.debug("Creating new output-stream to '{}'", outFile);
			outputStream = new PrintStream( new FileOutputStream(outFile));
		}
	}
	
	String Change_to_String( ArrayList<Integer>[] array) {
		String info = Arrays.toString(array);
		return info;
	}
	
	
	@Override
	public Data process(Data data) {
		
		PixPulseSize = (ArrayList<Integer>[]) data.get(PixPulseSizeKey);
		outputStream.println( Change_to_String(PixPulseSize) );
		System.out.println(PixPulseSize);	
		return data;
	}


	/**
	 * @see stream.io.CsvWriter#close()
	 */
	@Override
	public void finish() throws Exception {
		outputStream.close();
	}

	public String getPixPulseSizeKey() {
		return PixPulseSizeKey;
	}


	public void setPixPulseSizeKey(String pixPulseSizeKey) {
		PixPulseSizeKey = pixPulseSizeKey;
	}


	public String getUrlString() {
		return urlString;
	}


	public void setUrlString(String urlString) {
		this.urlString = urlString;
	}
			
	
}
