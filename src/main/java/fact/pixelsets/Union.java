package fact.pixelsets;

import com.google.common.collect.Sets;
import fact.Utils;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;


/**
 * This processor gets two pixel sets and performs a union of these sets.
 * Created by jebuss on 17.12.15.
 */
public class Union implements Processor {

    @Parameter(required = true, description = "key to the first set to be united")
    public String setAKey;

    @Parameter(required = true, description = "key to the second set to be united")
    public String setBKey;

    @Parameter(required = true, description = "key to the output set which contains the union")
    public String outsetKey;

    @Override
    public Data process(Data input) {

        Utils.isKeyValid(input, setAKey, PixelSet.class);
        Utils.isKeyValid(input, setBKey, PixelSet.class);

        PixelSet setA = (PixelSet) input.get(setAKey);
        PixelSet setB = (PixelSet) input.get(setBKey);

        Sets.SetView<CameraPixel> union = Sets.union(setA.set, setB.set);

        PixelSet outset = new PixelSet();
        union.copyInto(outset.set);
        input.put(outsetKey, outset);

        return input;
    }
}
