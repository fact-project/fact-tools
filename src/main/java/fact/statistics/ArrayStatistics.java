package fact.statistics;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;

public class ArrayStatistics implements Processor {
	static Logger log = LoggerFactory.getLogger(ArrayStatistics.class);
	private String key = null;
	private String outputKey = null;

	@Override
	public Data process(Data input) {
		// TODO Auto-generated method stub
		Utils.mapContainsKeys(this.getClass(), input, key);
		
		double[] data = Utils.toDoubleArray(input.get(key));
		
		int n = data.length;
		
		double max = 0;
		int max_pos = 0;
		double min = Double.MAX_VALUE;
		int min_pos = 0;
		double mean = 0;
		double rms = 0;
		double values = 0;
		double valuesSquare = 0;
		
		for (int i = 0 ; i < n ; i++)
		{
			double value = data[i];
			if (value > max)
			{
				max = value;
				max_pos = i;
			}
			if (value < min)
			{
				min = value;
				min_pos = i;
			}
			values += value;
			valuesSquare += value*value;
		}
		mean = values / n;
		rms = Math.sqrt( (valuesSquare - (values*values)/n)/(n-1) );
		
		input.put(outputKey+"Mean",mean);
		input.put(outputKey+"Min",min);
		input.put(outputKey+"MinPos",min_pos);
		input.put(outputKey+"Max",max);
		input.put(outputKey+"MaxPos",max_pos);
		input.put(outputKey+"Rms",rms);
		
		
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
