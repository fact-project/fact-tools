package fact.pixelsets;

import fact.Utils;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.io.Serializable;


/*
 *
 *
 */
public class FromChidArray implements Processor{
    static Logger log = LoggerFactory.getLogger(FromChidArray.class);

    @Parameter(required = true, description = "key to the input int array of chids")
    private String inputKey;

    @Parameter(required = true, description = "key to the output for the pixelSet")
    private String outputKey;

    @Override
    public Data process(Data input) {
        FactPixelMapping m = FactPixelMapping.getInstance();
        PixelSet pixelSet = new PixelSet();
        int[] chids = (int[]) input.get(inputKey);
        for(int chid : chids){
            pixelSet.add(m.getPixelFromId(chid));
        }
        input.put(outputKey, pixelSet);
        return input;
    }
}