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
 * This processor gets two pixel sets (A and B) and returns the symmetric difference of sets A and B,
 * denoted A △ B or A ⊖ B, is the set of all objects that are a member of exactly one of A and B
 * (elements which are in one of the sets, but not in both).
 * For instance, for the sets {1,2,3} and {2,3,4} , the symmetric difference set is {1,4} .
 * <p>
 * Created by jebuss on 17.12.15.
 */
public class SymmetricDifference implements Processor {
    static Logger log = LoggerFactory.getLogger(SymmetricDifference.class);

    @Parameter(required = true, description = "key to the first set to be compared")
    public String setAKey;

    @Parameter(required = true, description = "key to the second set to be united")
    public String setBKey;

    @Parameter(required = true, description = "key to the output set which contains the symmetric difference")
    public String outsetKey;

    @Override
    public Data process(Data item) {

        Utils.isKeyValid(item, setAKey, PixelSet.class);
        Utils.isKeyValid(item, setBKey, PixelSet.class);

        PixelSet setA = (PixelSet) item.get(setAKey);
        PixelSet setB = (PixelSet) item.get(setBKey);

        Sets.SetView<CameraPixel> symDiff = Sets.symmetricDifference(setA.set, setB.set);
        PixelSet outset = new PixelSet();
        symDiff.copyInto(outset.set);
        item.put(outsetKey, outset);

        return item;
    }
}
