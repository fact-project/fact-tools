package fact.pixelsets;

import fact.container.PixelSet;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;


/**
 * Convert an int array of chids into a PixelSet
 *
 */
public class AllPixels implements Processor{
    static Logger log = LoggerFactory.getLogger(AllPixels.class);

    @Parameter(required = false, description = "key to the output for the pixelSet")
    private String outputKey = "pixels";

    private FactPixelMapping mapping = FactPixelMapping.getInstance();

    @Override
    public Data process(Data input) {
        PixelSet pixelSet = new PixelSet();

        for(FactCameraPixel pix : mapping.pixelArray){
            pixelSet.add(pix);
        }
        input.put(outputKey, pixelSet);
        return input;
    }
}
