package fact.data;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Processor;
import stream.annotations.Parameter;
import stream.Data;
import fact.Constants;

/**
 * This operator simply multiplies all values by the given factor.
 * 
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class MultiplyValues implements Processor {
	static Logger log = LoggerFactory.getLogger(MultiplyValues.class);
	private float factor = -1;
	private boolean overwrite = false;
	private String output, key;

	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		if(output == null || output ==""){		
			overwrite = true;
			input.put(key, processEvent(input, key));
		} else {
			input.put(output, processEvent(input, key));
		}
		return input;
	}

	public float[] processEvent(Data input, String key) {

		Serializable value = null;

		if(input.containsKey(key)){
			value = input.get(key);
		} else {
			//key doesnt exist in map
			log.info(Constants.ERROR_WRONG_KEY + key + ",  " + this.getClass().getSimpleName() );
			return null;
		}

		if (value != null && value.getClass().isArray()
				&& value.getClass().getComponentType().equals(float.class)) {
			return processSeries((float[]) value);

		}
		//in case value in Map is of the wrong type to do this calculation
		else
		{
			log.info(Constants.EXPECT_ARRAY_F + key + ",  " + this.getClass().getSimpleName() );
			return null;
		}

	}

	
	public float[] processSeries(float[] data) {
		float[] arr = null;
		if(!overwrite){
			arr = new float[data.length];
		}
		int roi = data.length / Constants.NUMBEROFPIXEL;
		//foreach pixel
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			//iterate over all slices
			for (int slice = 0; slice < roi; slice++) {
				int pos = pix * roi + slice;
				if(overwrite){
					data[pos] = data[pos] * factor;
				} else {
					arr[pos]  = data[pos] * factor;
				}
				
			}
		}
		return data;
	}

	/*
	 * Getter and setter
	 */

	public float getFactor() {
		return factor;
	}
	@Parameter(required = false, description = "The factor by which to multiply", defaultValue="-1")
	public void setFactor(float threshold) {
		this.factor = threshold;
	}

	public String getOutput() {
		return output;
	}
	public void setOutput(String output) {
		this.output = output;
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
}
