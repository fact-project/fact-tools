package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class NumberOfPixelInShower implements Processor {
	static Logger log = LoggerFactory.getLogger(NumberOfPixelInShower.class);
    @Parameter(required = true)
	private String showerKey;
    @Parameter(required = true)
	private String outputKey;
	
	@Override
	public Data process(Data input) {
//		EventUtils.isKeyValid(getClass(),input, showerKey, int[].class);
		
		int length = 0;
		if (input.containsKey(showerKey))
		{
			int[] shower = (int[]) input.get(showerKey);
			length = shower.length;
		}
	    input.put(outputKey, length);
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
