package fact.processors;


import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Description;
import fact.Constants;
/**
 * This operator calculates the difference of all the slices in each Pixel between two arrays given by the keys key and keyB and stores the result as a double array.
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 *
 */

@Description(group = "Data Stream.FACT")
public class Diff implements Processor {
	static Logger log = LoggerFactory.getLogger(Diff.class);
	private String key, keyB;
	private String outputKey;
	
	private int offset=1;

	public Diff(){}
	public Diff(String key){
		this.key =  key;
	}
	

	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {
		if(outputKey == null || outputKey ==""){
			input.put(Constants.KEY_DIFF, processEvent(input, key));
		} else {
			input.put(outputKey, processEvent(input, key));
		}
		return input;
	}
	
	
	public float[] processEvent(Data input, String key) {
		
		Serializable valueA = null;
		
		if(input.containsKey(key)){
			 valueA = input.get(key);
		} else {
			//key doesnt exist in map
			log.info(Constants.ERROR_WRONG_KEY + key + ",  " + this.getClass().getSimpleName() );
			return null;
		}
		
		
		Serializable valueB = null;
		if(valueB == null) valueB = valueA;
		
		if(input.containsKey(key)){
			 valueB = input.get(key);
		} else {
			//key doesnt exist in map
			log.info(Constants.ERROR_WRONG_KEY + key + ",  " + this.getClass().getSimpleName() );
			return null;
		}
		
		if (!(valueA != null && valueA.getClass().isArray()
				&& valueA.getClass().getComponentType().equals(float.class)) ) {
			log.info(Constants.EXPECT_ARRAY_F + key + ",  " + this.getClass().getSimpleName() );
			return null;
		}
		if (!(valueB != null && valueB.getClass().isArray()
				&& valueB.getClass().getComponentType().equals(float.class)) ) {
			log.info(Constants.EXPECT_ARRAY_F + keyB + ",  " + this.getClass().getSimpleName() );
			return null;
		}
		if(((float[])valueA).length != ((float[])valueB).length){
			log.info("The arrays dont have the same length. ->  " + key+ ", " + keyB + ":  " +this.getClass().getSimpleName() );
		}
		return processSeries((float[])valueA, (float[])valueB);
		
	}


	public float[] processSeries(float [] seriesA, float[] seriesB){
		int roi = seriesA.length / Constants.NUMBEROFPIXEL;
		float[] diff = new float[seriesA.length];
		//Iterate over all Pixels in event.
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++){
			for (int slice = 0; slice < roi; slice++) {
				int pos = pix * roi + slice;
				int sliceB = slice + offset;
				if(sliceB>= 0 && sliceB < roi){
					int posB = pix * roi + sliceB;
					diff[pos] = seriesA[pos] - seriesB[posB];
				}
				if(sliceB < 0){
					diff[pos] = seriesA[pos] - seriesB[0];
				} else if(sliceB > roi){
					diff[pos] = seriesA[pos] - seriesB[roi-1];
				}
			}
		}
		return diff;

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
	
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	
	public String getKeyB() {
		return keyB;
	}
	public void setKeyB(String keyB) {
		this.keyB = keyB;
	}
	
	
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}

	
}
