/**
 * 
 */
package fact.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.Constants;

/**
 * 
 * This is a processor to cut slices of a Fact-Event in each Pixel. It takes the rawdata from a fact-event and cuts  of all \textit{slice}  
 * @author chris, niklaswulf, kai
 * 
 * 
 */
public class CutSlices implements Processor {
	static Logger log = LoggerFactory.getLogger(CutSlices.class);

	Integer start = 0;
	Integer end = 300;
	Integer elements = 1440;

	String[] keys = new String[] { "DataCalibrated" };

	private String outputKey;
	



	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data data) {

		for (String key : keys) {

			try{
				double[] original = (double[]) data.get(key);
				int rows = elements;
				
				int oldRoi = original.length / rows;
				int newRoi = (end - start);

				double[] result = new double[newRoi * rows];
				for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
					System.arraycopy(original, pix * oldRoi + start,
							result, pix * newRoi, newRoi);
				}
				data.put(outputKey, result);
				data.put("@start" + key, start);
				data.put("@end" + key, start);

			} catch(ClassCastException e){
				log.error("The key " + key + " does not refer to a double array." );
				throw new RuntimeException("This is not a double[]");
			}
		}
		return data;
	}

	
	public Integer getStart() {
		return start;
	}

	@Parameter(name = "start", defaultValue = "0", required = false)
	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getEnd() {
		return end;
	}

	@Parameter(name = "end", defaultValue = "300", required = false)
	public void setEnd(Integer end) {
		this.end = Math.max(start + 1, end);
	}

	public String[] getKeys() {
		return keys;
	}

	@Parameter(name = "keys", defaultValue = "Data", values = { "Data" }, required = false)
	public void setKeys(String[] keys) {
		this.keys = keys;
	}
	
	


	public String getOutputKey() {
		return outputKey;
	}
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}
}