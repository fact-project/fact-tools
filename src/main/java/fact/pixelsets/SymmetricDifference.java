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
 * This processor gets two pixel sets (A and B) and returns the symmetric difference of sets A and B,
 * denoted A △ B or A ⊖ B, is the set of all objects that are a member of exactly one of A and B
 * (elements which are in one of the sets, but not in both).
 * For instance, for the sets {1,2,3} and {2,3,4} , the symmetric difference set is {1,4} .
 *
 * Created by jebuss on 17.12.15.
 */
public class SymmetricDifference implements Processor{
    static Logger log = LoggerFactory.getLogger(SymmetricDifference.class);

    @Parameter(required = true, description = "key to the first set to be compared")
    private String setAKey;

    @Parameter(required = true, description = "key to the second set to be united")
    private String setBKey;

    @Parameter(required = true, description = "key to the output set which contains the symmetric difference")
    private String outsetKey;

    @Override
    public Data process(Data input) {

        PixelSetOverlay setA;
        PixelSetOverlay setB;

        //check if inset1 is given, otherwise create an empty set
        if (input.containsKey(setAKey)) {
            Utils.isKeyValid(input, setAKey, PixelSetOverlay.class);
            setA = (PixelSetOverlay) input.get(setAKey);
        } else {
            //create an empty set if no set is handed over
            setA = new PixelSetOverlay();
        }

        //check if inset2 is given, otherwise create an empty set
        if (input.containsKey(setBKey)) {
            Utils.isKeyValid(input, setBKey, PixelSetOverlay.class);
            setB = (PixelSetOverlay) input.get(setBKey);
        } else {
            //create an empty set if no set is handed over
            setB = new PixelSetOverlay();
        }

        //return if both input sets are empty
        if (setA.set.isEmpty() && setB.set.isEmpty()){
            return input;
        }

        try{
            Sets.SetView<CameraPixel> symDiff = Sets.symmetricDifference(setA.set, setB.set);
            Set<CameraPixel> cameraPixels = symDiff.immutableCopy();
            PixelSetOverlay outset = new PixelSetOverlay(cameraPixels);
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
