package fact.pixelsets;

import com.google.common.collect.Sets;
import fact.Utils;
import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;


/**
 * This processor gets two pixel sets (U and A) and returns the difference of these sets,
 * denoted U \ A, is the set of all members of U that are not members of A
 * Created by jebuss on 17.12.15.
 */
public class Difference implements Processor {
    static Logger log = LoggerFactory.getLogger(Difference.class);

    @Parameter(required = true, description = "key to the first set to be compared")
    public String setUKey;

    @Parameter(required = true, description = "key to the second set to be united")
    public String setAKey;

    @Parameter(required = true, description = "key to the output set which contains the difference")
    public String outsetKey;

    @Override
    public Data process(Data item) {

        Utils.isKeyValid(item, setUKey, PixelSet.class);
        Utils.isKeyValid(item, setAKey, PixelSet.class);

        PixelSet setU = (PixelSet) item.get(setUKey);
        PixelSet setA = (PixelSet) item.get(setAKey);

        Sets.SetView<CameraPixel> difference = Sets.difference(setU.set, setA.set);
        PixelSet outset = new PixelSet();
        difference.copyInto(outset.set);
        item.put(outsetKey, outset);

        return item;
    }
}
