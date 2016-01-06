package fact.features;

import fact.Utils;
import fact.hexmap.ui.overlays.PixelSetOverlay;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * If showerKey refers to an int[] of showerpixel. this will calculate the number of islands
 * @author kaibrugge
 *
 */
public class NumberOfIslands implements Processor {

    @Parameter(required = true, description = "Key refering to an array of integer containing pixel Ids")
    private String pixelSetKey;

    @Parameter(required = true)
    private String outputKey;

    @Override
    public Data process(Data input) {
    	if (!input.containsKey(pixelSetKey))
    	{
    		input.put(outputKey, 0);
    		return input;
    	}
        Utils.isKeyValid(input, pixelSetKey, PixelSetOverlay.class);

        PixelSetOverlay showerPixel = (PixelSetOverlay) input.get(pixelSetKey);
        int numIslands = Utils.breadthFirstSearch(showerPixel.toArrayList()).size();
        input.put(outputKey, numIslands);
        return input;
    }

    public void setPixelSetKey(String pixelSetKey) {
        this.pixelSetKey = pixelSetKey;
    }

    public void setOutputKey(String outputkey) {
        this.outputKey = outputkey;
    }

}
