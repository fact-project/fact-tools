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
 * This processor gets two pixel sets and performs a union of these sets.
 * Created by jebuss on 17.12.15.
 */
public class Union implements Processor{
    static Logger log = LoggerFactory.getLogger(Union.class);

    @Parameter(required = true, description = "key to the first set to be united")
    private String setAKey;

    @Parameter(required = true, description = "key to the second set to be united")
    private String setBKey;

    @Parameter(required = true, description = "key to the output set which contains the union")
    private String outsetKey;

    @Override
    public Data process(Data input) {

        if (!input.containsKey(setAKey)) {
            return input;
        }

        if (!input.containsKey(setBKey)) {
            return input;
        }

        Utils.isKeyValid(input, setAKey, PixelSetOverlay.class);
        Utils.isKeyValid(input, setBKey, PixelSetOverlay.class);

        PixelSetOverlay setA = (PixelSetOverlay) input.get(setAKey);
        PixelSetOverlay setB = (PixelSetOverlay) input.get(setBKey);

        try {
            Sets.SetView<CameraPixel> union = Sets.union(setA.set, setB.set);
            Set<CameraPixel> cameraPixels = union.immutableCopy();

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
