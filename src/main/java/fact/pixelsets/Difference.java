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
 * This processor gets two pixel sets (U and A) and returns the difference of these sets,
 * denoted U \ A, is the set of all members of U that are not members of A
 * Created by jebuss on 17.12.15.
 */
public class Difference implements Processor{
    static Logger log = LoggerFactory.getLogger(Difference.class);

    @Parameter(required = true, description = "key to the first set to be compared")
    private String inset1Key;

    @Parameter(required = true, description = "key to the second set to be united")
    private String inset2Key;

    @Parameter(required = true, description = "key to the output set which contains the difference")
    private String outsetKey;

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
            Sets.SetView<CameraPixel> difference = Sets.difference(inset1.set, inset2.set);
            Set<CameraPixel> cameraPixels = difference.immutableCopy();
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
