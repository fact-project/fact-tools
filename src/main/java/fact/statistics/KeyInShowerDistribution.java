package fact.statistics;

import fact.Utils;
import fact.container.PixelSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;

import java.io.Serializable;
public class KeyInShowerDistribution implements Processor {

	private String pixelSetKey;
	private String outputKey;
	private String Key
	;
	
	@Override
	public Data process(Data input) {
		
		final Logger log = LoggerFactory.getLogger(ArrayMean.class);
			
		Serializable serialData = input.get(Key);
		double[] keyData = Utils.toDoubleArray(serialData);
		int[] showerPixel = ((PixelSet) input.get(pixelSetKey)).toIntArray();
		
		if(showerPixel.length<=1){
			log.info("No Sample Standard Deviation defined for sample.length<2, filling up with -1");
			input.put(outputKey+"Mean", -1);
			input.put(outputKey+"StdDev", -1);
			return input;
		}
		
		double[] showerKey = new double[showerPixel.length];
		
		double meanKey = 0;
		double stdDevKey = 0;
		for(int i=0; i<showerPixel.length; i++){
			showerKey[i] = keyData[showerPixel[i]];
			meanKey += showerKey[i];
		}
		
		meanKey /= (double)showerKey.length;
		
		
		for(int i=0; i<showerPixel.length; i++){
			stdDevKey += Math.pow(showerKey[i] - meanKey, 2);
		}
		
		stdDevKey = Math.sqrt(1/(double)(showerKey.length -1) * stdDevKey);
		input.put(outputKey, keyData);
		input.put(outputKey+"Mean", meanKey);
		input.put(outputKey+"StdDev", stdDevKey);
		
		return input;
	}

	public String getPixelSetKey() {
		return pixelSetKey;
	}

	public void setPixelSetKey(String pixelSetKey) {
		this.pixelSetKey = pixelSetKey;
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
