package fact.pixelsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Utils;
import fact.container.PixelSet;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This processor calculates the length of a pixelset and put it in the data item.
 * 
 * @author ftemme,jbuss
 *
 */
public class NumPixel implements Processor {
	
	static Logger log = LoggerFactory.getLogger(NumPixel.class);
	
	@Parameter(required=true, description="The pixelSet of which you want to store the number of pixels in the outputKey")
	private String pixelSetKey;
	@Parameter(description="The outputKey", defaultValue="<pixelSetKey>:numPixel")
	private String outputKey = null;

	@Override
	public Data process(Data input) {

		if (outputKey == null){
			outputKey = pixelSetKey + ":numPixel";
		}
		
		Utils.isKeyValid(input, pixelSetKey, PixelSet.class);
		
		PixelSet pixelSet = (PixelSet) input.get(pixelSetKey);
		
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
