package fact.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
public class KeyInShowerDistribution implements Processor {

	private String showerPixelKey;
	private String outputKey;
	private String Key
	;
	
	@Override
	public Data process(Data input) {
		
		final Logger log = LoggerFactory.getLogger(ArrayMean.class);
			
		double[] keyData = (double[]) input.get(Key);
		int[] showerPixel = (int[]) input.get(showerPixelKey);
		
		if(showerPixel.length<=1){
			log.error("No Sample Standard Deviation defined for sample.length<2");
			return input;
		}
		
		double[] showerKey = new double[showerPixel.length];
		
		double meanKey = 0;
		double stdDevKey = 0;
		for(int i=0; i<showerPixel.length; i++){
			showerKey[i] = keyData[showerPixel[i]];
			meanKey += showerKey[i];
		}
		
		meanKey /= showerKey.length;
		
		for(int i=0; i<showerPixel.length; i++){
			showerKey[i] -= meanKey;
			stdDevKey += Math.pow(showerKey[i] - meanKey, 2);
		}
		
		stdDevKey = Math.sqrt(1/(showerKey.length -1) * stdDevKey);
		
		input.put(outputKey, keyData);
		input.put(outputKey+"Mean", meanKey);
		input.put(outputKey+"StdDev", stdDevKey);
		
		return input;
	}

	public String getShowerPixelKey() {
		return showerPixelKey;
	}

	public void setShowerPixelKey(String showerPixelKey) {
		this.showerPixelKey = showerPixelKey;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputkey(String outputKey) {
		this.outputKey = outputKey;
	}

	public String getKey() {
		return Key;
	}

	public void setKey(String Key) {
		this.Key = Key;
	}

}
