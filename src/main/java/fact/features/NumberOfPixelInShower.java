package fact.features;

import fact.hexmap.ui.overlays.PixelSetOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class NumberOfPixelInShower implements Processor {
	static Logger log = LoggerFactory.getLogger(NumberOfPixelInShower.class);
    @Parameter(required = true)
	private String pixelSetKey;
    @Parameter(required = true)
	private String outputKey;
	
	@Override
	public Data process(Data input) {
//		EventUtils.isKeyValid(input, showerKey, int[].class);
		
		int length = 0;
		if (input.containsKey(pixelSetKey))
		{
			int[] shower = ((PixelSetOverlay) input.get(pixelSetKey)).toIntArray();
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

	public void setPixelSetKey(String pixelSetKey) {
		this.pixelSetKey = pixelSetKey;
	}
}
