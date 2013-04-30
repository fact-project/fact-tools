/**
 * 
 */
package fact.processors.parfact;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import fact.processors.StdDeviation;

import stream.Processor;
import stream.annotations.Parameter;
import stream.Data;

/**
 * Supposedly removes all spikes in the data.
 * Original algorithm by F.Temme. 
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class RemoveSpikesMars implements Processor {
	static Logger log = LoggerFactory.getLogger(StdDeviation.class);
	private String key;
	private String output;
	//color that will be drawn in graph window
	private String color = "#1E9E08";

	public RemoveSpikesMars() {
	}
	public RemoveSpikesMars(String key) {
		this.key=key;
	}

	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {
		if(output == null || output ==""){
			input.put(key, processEvent(input, key));
		} else {
			input.put(output, processEvent(input, key));
			input.put("@"+Constants.KEY_COLOR+"_"+Constants.KEY_SPIKES_REMOVED, color);
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
	/**
	 * orginal c++ code and one liner comments by F.Temme. 
	 * Validity of array indices has not been checked
	 * @param data
	 * @return 
	 */

	public float[] processSeries(float[] data) {
		// Iterieren ueber alle Pixel
		int roi = data.length / Constants.NUMBEROFPIXEL;
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {
			//iterate over all slices
			for (int slice = 1; slice < roi-3; slice++) {
				int sl = pix * roi + slice;
				// check if it is a one slice jump up
				if (data[sl] - data[sl-1] > 25)
				{
					// check if immediately a one slice jump down follows
					// ==> Single Spike
					if(data[sl+1] - data[sl] < -25)
					{
						data[sl]     = ( data[sl-1] + data[sl+1] ) / 2;
					}
				}
				// check if it is a one slice jump up
				if (data[sl] - data[sl-1] > 22)
				{
					// check if immediately a small step follows
					if (Math.abs((data[sl+1] - data[sl])) < 4 )
					{
						// check if then a one slice jump down follows
						// ==> Double Spike
						if (data[sl+2] - data[sl+1] < -22)
						{
							data[sl] = ( data[sl-1] + data[sl+2] ) / 2;
							data[sl+1] = data[sl];
						}
					}
				}

			}
		}
		return data;
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

	//oragne
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


}
