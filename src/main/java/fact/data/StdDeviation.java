package fact.data;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;

import stream.Processor;
import stream.Data;

/**
 * This Processor calculates the Standarddeviation of the slices in each pixel. It uses the Average Processor to calculate the average value ina pixel. 
 * 
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class StdDeviation implements Processor {
	static Logger log = LoggerFactory.getLogger(StdDeviation.class);
	
	public StdDeviation() {
		
	}

	public StdDeviation(String[] keys) {
		this.keys=keys;
	}

	

	 /**
     * parameter and getter setters
     */
    String[] keys = new String[] { Constants.DEFAULT_KEY };
	private boolean overwrite;

	
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

	
	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		//
		//
		//String[] keys = new String[] { "Data", "DataCalibrated" };
		
		for (String key : keys) {
			if(overwrite){
				input.put(Constants.KEY_STD, processEvent(input, key));
			} else {
				input.put(Constants.KEY_STD+key, processEvent(input, key));
			}
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
		//in case value in Map is of the wrong type to do this calculation
		else
		{
			return null;
		}
		
	}

	public double[] processSeries(float[] data) {
		//get the average value in each pixel
		double[] avgs = new PixelAverage().processSeries(data);
		
		double[] stds = new double[avgs.length];
		
		int roi = data.length / Constants.NUMBEROFPIXEL;
		double difference = 0.0f;
		//foreach pixel
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			//iterate over all slices
			for (int slice = 1; slice < roi; slice++) {
				int pos = pix * roi + slice;
				difference += Math.pow((data[pos]- avgs[pix]),2);
			}
			stds[pix] = Math.sqrt((1/((double)data.length - 1)) * difference); 
			difference = 0.0f;
		}
		return stds;
	}
}


