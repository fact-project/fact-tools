/**
 * 
 */
package fact.data;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;

import stream.AbstractProcessor;
import stream.Data;

/**
 * @author chris
 * 
 */
public class SliceNormalization extends AbstractProcessor {

	static Logger log = LoggerFactory.getLogger(SliceNormalization.class);

	String[] keys = new String[] { "Data" };
	
	public SliceNormalization(){}
	public SliceNormalization(String[] keys){
		this.keys =  keys;
	}

	/**
	 * @return the keys
	 */
	public String[] getKeys() {
		return keys;
	}

	/**
	 * @param keys
	 *            the keys to set
	 */
	public void setKeys(String[] keys) {
		this.keys = keys;
	}
	
	private boolean overwrite = true;

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		for (String key : keys) {
			if(overwrite){
				input.put(key, processEvent(input, key));
			} else {
				input.put(Constants.KEY_NORMALIZED_SLICES + "_" + key, processEvent(input, key));
			}
		}

		return input;
	}
	


	public float[] processEvent(Data input,  String key) {
		Serializable value = null;
		
		if(input.containsKey(key)){
			 value = input.get(key);
		} else {
			//key doesnt exist in map
			return null;
		}
		
		if (value != null && value.getClass().isArray()
				&& value.getClass().getComponentType().equals(float.class)) {
			return processSeries((float[]) value);
			
		}
		//in case value in Map is of the wrong type to do this calculation
		else
		{
			return null;
		}


	
	}
	
	public float[] processSeries(float[] value){
		log.debug("Normalizing image array (key: {})...");

		float[] image =  value;
		float[] normalizedSlices =  new float[image.length];
		
		
		int pixels = 1440;
		int roi = image.length / pixels;

		
		for (int pix = 0; pix < pixels; pix++) {
			float min = Float.MAX_VALUE;
			float max = Float.MIN_VALUE;
			for (int slice = 0; slice < roi; slice++) {
				int pos = pix * roi + slice;
				min = Math.min(min, image[pos]);
				if (max < image[pos]) {
					max = image[pos];
				}
			}

			float range = max - min;
			
			for (int slice = 0; slice < roi; slice++) {
				int pos = pix * roi + slice;
				normalizedSlices[pos] = (Math.abs(min) + image[pos]) / (2 * range);
			}
		}
		return normalizedSlices;
	}
	
	
	
	public boolean isOverwrite() {
		return overwrite;
	}
	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}
}
