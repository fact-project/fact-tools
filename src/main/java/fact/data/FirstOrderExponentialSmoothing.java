package fact.data;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import stream.Processor;
import stream.annotations.Parameter;
import stream.Data;


/**
 * Calculates first Order exponential Smoothing
 * Let y be the original Series and s be the smoothed one.
 *  s_0 = y_0
 *  s_i = alpha*y_i + (1-alpha) * s_(i-1)
 *  see http://en.wikipedia.org/wiki/Exponential_smoothing
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 *
 */
public class FirstOrderExponentialSmoothing implements Processor {

	
	static Logger log = LoggerFactory.getLogger(FirstOrderExponentialSmoothing.class);
	float[] data;
	float alpha;
	private String color = "#D1A28A";
	private String output;
	private String key;


	
	
	@Override
	public Data process(Data input) {
		if(output == null || output ==""){
			input.put(key, processEvent(input, key));
		} else {
			input.put(output, processEvent(input, key));
			input.put("@"+Constants.KEY_COLOR+"_"+Constants.KEY_EXPONENTIALY_SMOOTHED, color);
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
		else
		{
			log.info(Constants.EXPECT_ARRAY_F + key + ",  " + this.getClass().getSimpleName() );
			return null;
		}

	}


	public float[] processSeries(float[] data) {
			int roi = data.length / Constants.NUMBEROFPIXEL;
			float[] smoothedData= new float[data.length];
			//foreach pixel
			for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
				//float maximum = data[pix * roi];
				//beginn with startvalue 
				smoothedData[pix*roi] = data[pix*roi];  
				for (int slice = 1; slice < roi; slice++) {
					int pos = pix * roi + slice;
					//glaettung
					smoothedData[pos] = alpha*data[pos] + (1-alpha)*smoothedData[pos-1];
				}
			}
		return smoothedData;
	}

	
	/*
	 * Getter and Setter
	 */
	public float getAlpha() {
		return alpha;
	}
	@Parameter (required = false, description = "This value changes the amount of smoothing that will take place. If alpha equals 1 the values remain unchanged.  See http://en.wikipedia.org/wiki/Exponential_smoothing", min = 0.0 , max = 1.0)
	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	//brownish
	public String getColor() {
		return color;
	}
	@Parameter(required = false, description = "RGB/Hex description String for the color that will be drawn in the FactViewer ChartPanel")
	public void setColor(String color) {
		this.color = color;
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
