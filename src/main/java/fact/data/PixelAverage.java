package fact.data;


import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Description;
import fact.Constants;
/**
 * This operator calculates the average of all the slices in each Pixel and stores the result as a double array.
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 *
 */

@Description(group = "Data Stream.FACT")
public class PixelAverage implements Processor {
	static Logger log = LoggerFactory.getLogger(PixelAverage.class);
	private String key;
	private String output;

	public PixelAverage(){}
	public PixelAverage(String key){
		this.key =  key;
	}
	

	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {
		if(output == null || output ==""){
			input.put(Constants.KEY_AVERAGES, processEvent(input, key));
		} else {
			input.put(output, processEvent(input, key));
		}
		return input;
	}
	
	
	public double[] processEvent(Data input, String key) {
		
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


	public double[] processSeries(float [] series){
		double[] averages= new double[Constants.NUMBEROFPIXEL];
		int roi = series.length / Constants.NUMBEROFPIXEL;
		//Iterate over all Pixels in event.
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++){
			//currentPixel = event.getPixels()[pix];
			float avg = 0.0f;
			//smoothedData[pix*roi] = data[pix*roi];  
			
			for (int slice = 0; slice < roi; slice++) {
				int pos = pix * roi + slice;
				avg+=series[pos];
			}
			//calculate Average over all timeslices
			averages[pix] =avg/roi;
		}
		return averages;

	}
	
	/*
	 * Getter and Setter
	 */
	
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
