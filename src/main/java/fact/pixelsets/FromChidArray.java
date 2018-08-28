package fact.pixelsets;

import fact.container.PixelSet;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;


/**
 * Convert an int array of chids into a PixelSet
 */
public class FromChidArray implements Processor {
    static Logger log = LoggerFactory.getLogger(FromChidArray.class);

    @Parameter(required = true, description = "key to the input int array of chids")
    public String inputKey;

    @Parameter(required = true, description = "key to the output for the pixelSet")
    public String outputKey;

    @Override
    public Data process(Data item) {
        FactPixelMapping m = FactPixelMapping.getInstance();
        PixelSet pixelSet = new PixelSet();
        int[] chids = (int[]) item.get(inputKey);
        for (int chid : chids) {
            pixelSet.add(m.getPixelFromId(chid));
        }
        item.put(outputKey, pixelSet);
        return item;
    }

}
