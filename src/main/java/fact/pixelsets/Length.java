package fact.pixelsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Utils;
import fact.container.PixelSetOverlay;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This processor calculates the length of a pixelset and put it in the data item.
 * 
 * @author ftemme,jbuss
 *
 */
public class Length implements Processor {
	
	static Logger log = LoggerFactory.getLogger(Length.class);
	
	@Parameter(required=true, description="The pixelset of which you want to calculate the length")
	private String pixelSetKey;
	@Parameter(required=true, description="The outputkey.")
	private String outputKey;

	@Override
	public Data process(Data input) {
		
		Utils.isKeyValid(input, pixelSetKey, PixelSetOverlay.class);
		
		PixelSetOverlay pixelSet = (PixelSetOverlay) input.get(pixelSetKey);
		
		int length = pixelSet.set.size();
		input.put(outputKey, length);
		
		return input;
	}

	public void setPixelSetKey(String pixelSetKey) {
		this.pixelSetKey = pixelSetKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

}
