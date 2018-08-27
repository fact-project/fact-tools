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
 * This processor gets two pixel sets and performs a intersection of the sets A and B, denoted A ∩ B,
 * is the set of all objects that are members of both A and B.
 * The intersection of {1, 2, 3} and {2, 3, 4} is the set {2, 3} .
 * <p>
 * Created by jebuss on 04.01.16.
 */
public class Intersection implements Processor {
    static Logger log = LoggerFactory.getLogger(Intersection.class);

    @Parameter(required = true, description = "key to the first set to intersect")
    public  String setAKey;

    @Parameter(required = true, description = "key to the second set to intersect")
    public String setBKey;

    @Parameter(required = true, description = "key to the output set which contains the intersection")
    public String outsetKey;


    @Override
    public Data process(Data item) {

        Utils.isKeyValid(item, setAKey, PixelSet.class);
        Utils.isKeyValid(item, setBKey, PixelSet.class);

        PixelSet setA = (PixelSet) item.get(setAKey);
        PixelSet setB = (PixelSet) item.get(setBKey);

        Sets.SetView<CameraPixel> intersection = Sets.intersection(setA.set, setB.set);

        PixelSet outset = new PixelSet();
        intersection.copyInto(outset.set);
        item.put(outsetKey, outset);

        return item;

    }
}
