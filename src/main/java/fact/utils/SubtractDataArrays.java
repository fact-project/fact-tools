/**
 * 
 */
package fact.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Subtract two Pixel Arrays of the same size from each other
 * 
 * @author jbuss
 *
 */
public class SubtractDataArrays implements Processor {
	static Logger log = LoggerFactory.getLogger(RemappingKeys.class);
	
    @Parameter(required = true, description = "The key to your data array.")
    private String key;
    @Parameter(required = true, description = "The key to your subtracted data array.")
    private String subtractedKey;
    @Parameter(required = false)
    private String outputKey;
    
    

	/* (non-Javadoc)
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {
		Utils.isKeyValid(input, key, double[].class);
		Utils.isKeyValid(input, subtractedKey, double[].class);
		
		double[] subtractedArray =  new double[Constants.NUMBEROFPIXEL];
		
		double[] array1 	 = (double[]) input.get(key);
		double[] array2 	 = (double[]) input.get(subtractedKey);
		
		for(int pix = 0 ; pix < Constants.NUMBEROFPIXEL; pix++){
			subtractedArray[pix] = array1[pix] - array2[pix];
		}
		
		//add times over threshold
		input.put(outputKey, subtractedArray);
		
		
		return input;
	}



	public static Logger getLog() {
		return log;
	}



	public static void setLog(Logger log) {
		SubtractDataArrays.log = log;
	}



	public String getKey() {
		return key;
	}



	public void setKey(String key) {
		this.key = key;
	}



	public String getSubtractedKey() {
		return subtractedKey;
	}



	public void setSubtractedKey(String subtractedKey) {
		this.subtractedKey = subtractedKey;
	}



	public String getOutputKey() {
		return outputKey;
	}



	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}
	
	

}
