package fact.pixelsets;

import fact.Utils;
import fact.container.PixelSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;


/**
 * This processor creates an int array with the chids
 * from the pixelSet.
 * created by maxnoe
 */
public class ToChidArray implements Processor {
    static Logger log = LoggerFactory.getLogger(ToChidArray.class);

    @Parameter(required = true, description = "key to the input PixelSet")
    public String pixelSetKey;

    @Parameter(required = true, description = "key to the output the chid array")
    public String outputKey;

    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, pixelSetKey, PixelSet.class);
        PixelSet pixelSet = (PixelSet) item.get(pixelSetKey);
        item.put(outputKey, pixelSet.toIntArray());
        return item;
    }
}
