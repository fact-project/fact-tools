package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import fact.EventUtils;
import stream.annotations.Parameter;

public class NumberOfPixelInShower implements Processor {
	static Logger log = LoggerFactory.getLogger(NumberOfPixelInShower.class);
    @Parameter(required = true)
	private String showerKey;
    @Parameter(required = true)
	private String outputKey;
	
	@Override
	public Data process(Data input) {
		EventUtils.isKeyValid(getClass(),input, showerKey, int[].class);
	
		int[] shower = (int[]) input.get(showerKey);
	    input.put(outputKey, shower.length);
		return input;
	}

//getter and setter for keys passed in the xml
	public String getOutputKey() {
		return outputKey;
	}
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}


	public String getShowerKey() {
		return showerKey;
	}
	public void setShowerKey(String showerKey) {
		this.showerKey = showerKey;
	}


}
