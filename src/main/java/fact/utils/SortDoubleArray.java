package fact.utils;

import java.util.Arrays;

import fact.EventUtils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class SortDoubleArray implements Processor {
	public String key = "";
	public String outputKey = "";
	
	@Override
	public Data process(Data input) {
		try{
		EventUtils.mapContainsKeys(getClass(), input, key);
		double [] array = (double[]) input.get(key);
		Arrays.sort(array);
		input.put(outputKey, array);
		}catch(Exception e)
		{
			input.put(null, outputKey);
		}
		
		return input;
	}

	public String getKey() {
		return key;
	}
	@Parameter(required = true, description="Input key to 1 dimensional double array")
	public void setKey(String key) {
		this.key = key;
	}

	public String getOutputKey() {
		return outputKey;
	}
	
	@Parameter(required = true, description = "Outputkey")
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

}
