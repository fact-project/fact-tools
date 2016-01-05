package fact.pixelsets;

import com.google.common.collect.Sets;
import fact.Utils;
import fact.hexmap.CameraPixel;
import fact.hexmap.ui.overlays.PixelSetOverlay;
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
    private String inset1Key;

    @Parameter(required = true, description = "key to the second set to intersect")
    private String inset2Key;

    @Parameter(required = true, description = "key to the output set which contains the intersection")
    private String outsetKey;
    {}
    @Override
    public Data process(Data input) {

        if (!input.containsKey(inset1Key)) {
            return input;
        }

        if (!input.containsKey(inset2Key)) {
            return input;
        }

        Utils.isKeyValid(input, inset1Key, PixelSetOverlay.class);
        Utils.isKeyValid(input, inset2Key, PixelSetOverlay.class);

        PixelSetOverlay inset1 = (PixelSetOverlay) input.get(inset1Key);
        PixelSetOverlay inset2 = (PixelSetOverlay) input.get(inset2Key);

        try{
            Sets.SetView<CameraPixel> intersection = Sets.intersection(inset1.set, inset2.set);
            Set<CameraPixel> cameraPixels = intersection.immutableCopy();

            PixelSetOverlay outset = new PixelSetOverlay(cameraPixels);

            input.put(outsetKey, outset);
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        return input;
    }

    public void setInset1Key(String inset1Key) {
        this.inset1Key = inset1Key;
    }

    public void setInset2Key(String inset2Key) {
        this.inset2Key = inset2Key;
    }

    public void setOutsetKey(String outsetKey) {
        this.outsetKey = outsetKey;
    }
}
