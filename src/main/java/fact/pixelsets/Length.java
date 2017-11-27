package fact.pixelsets;

import fact.Utils;
import fact.container.PixelSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	String pixelSetKey;
	@Parameter(required=true, description="The outputkey.")
	String outputKey;

	@Override
	public Data process(Data input) {

		Utils.isKeyValid(input, pixelSetKey, PixelSet.class);

		PixelSet pixelSet = (PixelSet) input.get(pixelSetKey);

		int length = pixelSet.set.size();
		input.put(outputKey, length);

		return input;
	}
}
