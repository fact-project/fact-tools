package fact.features;

import fact.Utils;
import fact.container.PixelSet;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * If showerKey refers to an int[] of showerpixel. this will calculate the number of islands
 *
 * @author kaibrugge
 */
public class NumberOfIslands implements Processor {

    @Parameter(required = true, description = "Key refering to an array of integer containing pixel Ids")
    public String pixelSetKey;

    @Parameter(required = true)
    public String outputKey;

    @Override
    public Data process(Data input) {
        if (!input.containsKey(pixelSetKey)) {
            input.put(outputKey, 0);
            return input;
        }
        Utils.isKeyValid(input, pixelSetKey, PixelSet.class);

        PixelSet showerPixel = (PixelSet) input.get(pixelSetKey);
        int numIslands = Utils.breadthFirstSearch(showerPixel).size();
        input.put(outputKey, numIslands);
        return input;
    }
}
