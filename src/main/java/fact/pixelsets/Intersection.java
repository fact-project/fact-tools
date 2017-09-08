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

import java.util.Set;


/**
 * This processor gets two pixel sets and performs a intersection of the sets A and B, denoted A ∩ B,
 * is the set of all objects that are members of both A and B.
 * The intersection of {1, 2, 3} and {2, 3, 4} is the set {2, 3} .
 *
 * Created by jebuss on 04.01.16.
 */
public class Intersection implements Processor{
    static Logger log = LoggerFactory.getLogger(Intersection.class);

    @Parameter(required = true, description = "key to the first set to intersect")
    private String setAKey;

    @Parameter(required = true, description = "key to the second set to intersect")
    private String setBKey;

    @Parameter(required = true, description = "key to the output set which contains the intersection")
    private String outsetKey;
    {}
    @Override
    public Data process(Data input) {

        if (!input.containsKey(setAKey)) {
            return input;
        }

        if (!input.containsKey(setBKey)) {
            return input;
        }

        Utils.isKeyValid(input, setAKey, PixelSet.class);
        Utils.isKeyValid(input, setBKey, PixelSet.class);

        PixelSet setA = (PixelSet) input.get(setAKey);
        PixelSet setB = (PixelSet) input.get(setBKey);

        try{
            Sets.SetView<CameraPixel> intersection = Sets.intersection(setA.set, setB.set);
            Set<CameraPixel> cameraPixels = intersection.immutableCopy();

            PixelSet outset = new PixelSet(cameraPixels);

            input.put(outsetKey, outset);
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        return input;
    }

    public void setSetAKey(String setAKey) {
        this.setAKey = setAKey;
    }

    public void setSetBKey(String setBKey) {
        this.setBKey = setBKey;
    }

    public void setOutsetKey(String outsetKey) {
        this.outsetKey = outsetKey;
    }
}
