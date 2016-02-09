package fact.features;

import fact.Utils;
import fact.container.PixelSet;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Calculate the number of disconnected graph components in the pixelSet.
 * @author kaibrugge
 */
public class NumberOfIslands implements Processor {

    @Parameter(required = false, description = "Key refering to the pixelSet to calculate the number of islands on")
    private String pixelSetKey = "shower";

    @Parameter(required = false)
    private String outputKey = "shower:numIslands";

    @Override
    public Data process(Data input) {
    	if (!input.containsKey(pixelSetKey))
    	{
    		input.put(outputKey, 0);
    		return input;
    	}
        Utils.isKeyValid(input, pixelSetKey, PixelSet.class);

        PixelSet showerPixel = (PixelSet) input.get(pixelSetKey);
        int numIslands = Utils.breadthFirstSearch(showerPixel.toArrayList()).size();
        input.put(outputKey, numIslands);
        return input;
    }

}
