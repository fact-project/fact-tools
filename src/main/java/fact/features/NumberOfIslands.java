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
    private String showerKey;

    @Parameter(required = true)
    private String outputKey;

    @Override
    public Data process(Data input) {
    	if (!input.containsKey(showerKey))
    	{
    		input.put(outputKey, 0);
    		return input;
    	}
        Utils.isKeyValid(input, showerKey, PixelSetOverlay.class);

        int[] showerPixel = ((PixelSetOverlay) input.get(showerKey)).toIntArray();
        int numIslands = Utils.breadthFirstSearch(Utils.arrayToList(showerPixel)).size();
        input.put(outputKey, numIslands);
        return input;
    }

    public String getShowerKey() {
		return showerKey;
	}

	public void setShowerKey(String showerKey) {
		this.showerKey = showerKey;
	}

	public void setOutputKey(String outputkey) {
        this.outputKey = outputkey;
    }

}
