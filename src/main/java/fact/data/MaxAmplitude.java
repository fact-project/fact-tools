/**
 * 
 */
package fact.data;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import fact.Constants;

/**
 * This processor simply calculates the maximum value and its position for all time slices in each Pixel.  
 * 
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class MaxAmplitude implements Processor {
	static Logger log = LoggerFactory.getLogger(MaxAmplitude.class);
	private float minValue= -5;
	private float maxValue = 3000;
	private String key;
	private String output;
	//This array will contain the maximum Amplitude for each pixel
	float[] amplitudes = null;
	// and their positions
	int[] positions =  null;


	public MaxAmplitude() {
	}

	public MaxAmplitude(String key) {
		this.key=key;
	}
	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		if(output == null || output ==""){		
			input.put(Constants.KEY_MAX_AMPLITUDE_POSITIONS, processEvent(input, key));
			input.put(Constants.KEY_MAX_AMPLITUDES, amplitudes);
		} else {
			input.put(output+"_positions", processEvent(input, key));
			input.put(output, amplitudes);
		}
		return input;
	}

	public int[] processEvent(Data input, String key) {

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

	public int[] processSeries(float[] series) {
		amplitudes = new float[Constants.NUMBEROFPIXEL];
		positions =  new int[Constants.NUMBEROFPIXEL];
		float[] data = series;
		int roi = data.length / Constants.NUMBEROFPIXEL;
		//foreach pixel
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			//initiate maxValue and postion
			float tempMaxValue = 0;
			int position = 0;
			//iterate over all slices
			for (int slice = 0; slice < roi; slice++) {
				int pos = pix * roi + slice;
				//temp save the current value
				float value = data[pos];
				//update maxvalue and position if current value exceeds old value and is still below the threshold set by the user
				if( value > tempMaxValue && value <= maxValue && value >= minValue ){
					tempMaxValue = value;
					position = slice;
				}
			}
			amplitudes[pix] = tempMaxValue;
			positions[pix] = position;  
		}
		return positions;
	}


	/*
	 * Getter and Setter
	 */
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getOutput() {
		return output;
	}
	public void setOutput(String output) {
		this.output = output;
	}
	
	public float getMinValue() {
		return minValue;
	}
	public void setMinValue(float minValue) {
		this.minValue = minValue;
	}
	
	public float getMaxValue() {
		return maxValue;
	}
	public void setMaxValue(float maxValue) {
		this.maxValue = maxValue;
	}


}
