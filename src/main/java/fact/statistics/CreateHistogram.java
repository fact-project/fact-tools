package fact.statistics;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;

import java.io.Serializable;


/**
 * Takes a float[] and returns an int[]
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 *
 */
public class CreateHistogram implements StatefulProcessor {
	static Logger log = LoggerFactory.getLogger(CreateHistogram.class);
	private int numberOfBins = 12;
	private float max = 500;
	private float min = 0;
	int[] bin;
	private String key;
	private String outputKey;
	
	@Override
	public void init(ProcessContext context){
		//number of buckets and over- and underflow bins
		bin = new int[numberOfBins + 2];
		
	}
		
	@Override
	public Data process(Data input) {
		if ( key != null && !input.containsKey(key)){
			log.error("Key "  + key + " not found in getColorFromValue");
			return null;
		}
		Serializable data = input.get(key);
		double[] dataArray;
		if(data.getClass().isArray()){
			dataArray = Utils.toDoubleArray(data);
			for(double f: dataArray){
				int index = 0;
				if(f < min){
					//thius goes into the underflow bin
					index = 0;
				} else if( f > max){
					//this goes into the overflow bin
					index = numberOfBins + 1;
				} else {
					index= (int)( (f/max)*numberOfBins + 1);
				}
				bin[index]++;
			}
		} else {
			Double d = Utils.valueToDouble(data);
			int index= (int)( (d.doubleValue()/max)*numberOfBins + 1);
			bin[index]++;
		}
		input.put(outputKey, bin);
//		System.out.println("bin length: " + bin.length);
		return input;
	}
	@Override
	public void resetState() throws Exception {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void finish() throws Exception {
		// TODO Auto-generated method stub
		
	}

	
	
	public int getNumberOfBins() {
		return numberOfBins;
	}
	public void setNumberOfBins(int numberOfBins) {
		this.numberOfBins = numberOfBins;
	}
	
	
	
	public float getMax() {
		return max;
	}
	public void setMax(float maxBin) {
		this.max = maxBin;
	}
	
	
	
	public float getMin() {
		return min;
	}
	public void setMin(float minbin) {
		this.min = minbin;
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
