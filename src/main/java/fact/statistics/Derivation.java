package fact.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.EventUtils;
import stream.Data;
import stream.Processor;

public class Derivation implements Processor {
	static Logger log = LoggerFactory.getLogger(Derivation.class);
	
	String key=null;
	String outputKey=null;
	@Override
	public Data process(Data input) {
		// TODO Auto-generated method stub
		EventUtils.mapContainsKeys(this.getClass(), input,key);
		
		double[] data = (double[])input.get(key);
		double[] result = new double[data.length];
		
		for (int i=1 ; i < data.length ; i++)
		{
			result[i] = data[i] - data[i-1];
		}
		
		input.put(outputKey, result);
		
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
