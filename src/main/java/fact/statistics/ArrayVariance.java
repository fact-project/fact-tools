package fact.statistics;

import java.io.Serializable;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import fact.data.EventUtils;

/**
 * This operator calculates the rms of the array specified by the key
 * 
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class ArrayVariance implements Processor {
	static Logger log = LoggerFactory.getLogger(ArrayVariance.class);
	private String key;
	private String outputKey = "rms";
	
	@Override
	public Data process(Data input) {
		EventUtils.isKeyValid(getClass(), input, key, Double[].class);		
		Serializable data = input.get(key);
		DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(EventUtils.toDoubleArray(data));
		
		//get the sqrt of the sum of squares. Lets call it RMS. Cause we can.
		input.put(outputKey , descriptiveStatistics.getVariance());
		return input;
	}

	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}

	
	public String getOutputKey() {
		return outputKey;
	}
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}
}
