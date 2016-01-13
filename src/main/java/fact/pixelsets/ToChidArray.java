package fact.pixelsets;

import com.google.common.collect.Sets;
import fact.Utils;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactCameraPixel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.Set;


/**
 * This processor gets two pixel sets (U and A) and returns the difference of these sets,
 * denoted U \ A, is the set of all members of U that are not members of A
 * Created by jebuss on 17.12.15.
 */
public class ToChidArray implements Processor{
    static Logger log = LoggerFactory.getLogger(ToChidArray.class);

    @Parameter(required = true, description = "key to the input PixelSet")
    private String pixelSetKey;

    @Parameter(required = true, description = "key to the output the chid array")
    private String outputKey;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, pixelSetKey, PixelSet.class);
        PixelSet pixelSet = (PixelSet) input.get(pixelSetKey);
        input.put(outputKey, pixelSet.toIntArray());
        return input;
    }
}
