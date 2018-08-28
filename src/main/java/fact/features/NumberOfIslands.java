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
    public Data process(Data item) {
        if (!item.containsKey(pixelSetKey)) {
            item.put(outputKey, 0);
            return item;
        }
        Utils.isKeyValid(item, pixelSetKey, PixelSet.class);

        PixelSet showerPixel = (PixelSet) item.get(pixelSetKey);
        int numIslands = Utils.breadthFirstSearch(showerPixel).size();
        item.put(outputKey, numIslands);
        return item;
    }
}
