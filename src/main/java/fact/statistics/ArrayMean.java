package fact.statistics;

import java.io.Serializable;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import fact.EventUtils;

/**
 * This operator calculates the mean value of the values in of the array specified by the key.
 * 
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class ArrayMean implements Processor {
	static Logger log = LoggerFactory.getLogger(ArrayMean.class);
	private String key;
	private String outputKey = "mean";
	
	@Override
	public Data process(Data input) {
		if(input.containsKey(key)){
			Serializable data = input.get(key);
			DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(EventUtils.toDoubleArray(data));
			
			input.put(outputKey , descriptiveStatistics.getMean());
			input.put(outputKey+ "_deviation" , descriptiveStatistics.getStandardDeviation());
			input.put(outputKey+ "_N" , descriptiveStatistics.getN());
			return input;
		} else {
			throw new RuntimeException("Key not found in event. "  + key  );
		}
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
