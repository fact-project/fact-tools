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
 * outputs a file containing a nested list
 * 
 * @author Katie Gray &lt;kathryn.gray@tu-dortmund.de&gt;
 * 
 */

@Description(group = "Data Stream.Output", text = "")
public class CreateNestedListFile extends CsvWriter {
	static Logger log = LoggerFactory.getLogger(CreateNestedListFile.class);
	PrintStream outputStream;
	
	@Parameter(required=true, description="ArrayList<Double>[] -  matrix of size NUMBEROFPIXEL containing lists of data for all pixels")
	private String PixPulseSizeKey;
    ArrayList<Integer>[] PixPulseSize = null;
    	//Pulse size was specific to my use, but this file will work with whatever measurement in the form of a matrix of lists.
    
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
	
	//the function which creates a string from the array
	String Change_to_String( ArrayList<Integer>[] array) {
		String info = Arrays.toString(array);
		return info;
	}
	
	
	@Override
	public Data process(Data data) {
		PixPulseSize = (ArrayList<Integer>[]) data.get(PixPulseSizeKey);
		outputStream.println( Change_to_String(PixPulseSize) );
		return data;
	}
	
	
	

	//Getters and Setters
	
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
