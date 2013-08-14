package fact.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Description;
/**
 * This operator calculates the difference of all the slices in each Pixel between two arrays given by the keys keyA and keyB and stores the result as a float array named outputKey.
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 *
 */

@Description(group = "Data Stream.FACT")
public class Diff implements Processor {
	static Logger log = LoggerFactory.getLogger(Diff.class);
	private String keyA, keyB;
	private String outputKey;

	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {
		
		if(!(input.containsKey(keyA) && input.containsKey(keyB))){
			log.error("map does not contain the right keys");
			return null;
		}
		if(keyA == null || keyB == null){
			log.error("You did not specify keyA or keyB");
			return null;
		}
		
		try {
			float[] a = (float[]) input.get(keyA);
			float[] b = (float[]) input.get(keyB);
			
			float[] result;
			if (outputKey == null){
				outputKey = keyA;
				result = a;
			} else {
				result = new float[a.length];
			}
			for(int i = 0; i < a.length; ++i){
				result[i] = a[i] - b[i];
			}
			input.put(outputKey, result);

		} catch(ClassCastException e){
			log.error("Could not cast the keys in the map to float arrays");
		} catch(ArrayIndexOutOfBoundsException e){
			log.error("Index out of bounds. The keyA has to refer to an array of length <= the lenght of array from keyB");
		}
		return input;
	}

	
	/*
	 * Getter and Setter
	 */
	
	public String getOutputKey() {
		return  outputKey;
	}
	public void setOutputKey(String output) {
		this.outputKey = output;
	}
	
	
	public String getKeyA() {
		return keyA;
	}
	public void setKeyA(String keyA) {
		this.keyA = keyA;
	}
	
	
	public String getKeyB() {
		return keyB;
	}
	public void setKeyB(String keyB) {
		this.keyB = keyB;
	}
}
