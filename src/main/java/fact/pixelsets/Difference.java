package fact.pixelsets;

import com.google.common.collect.Sets;
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
public class Difference implements Processor{
    static Logger log = LoggerFactory.getLogger(Difference.class);

    @Parameter(required = true, description = "key to the first set to be united")
    private String inset1Key;

    @Parameter(required = true, description = "key to the second set to be united")
    private String inset2Key;

    @Parameter(required = true, description = "key to the output set which contains the union")
    private String outsetKey;

    private PixelSetOverlay cleanedPixelSet;

    @Override
    public Data process(Data input) {

        PixelSetOverlay inset1 = (PixelSetOverlay) input.get(inset1Key);
        PixelSetOverlay inset2 = (PixelSetOverlay) input.get(inset2Key);

        Sets.SetView<CameraPixel> union = Sets.union(inset1.set, inset2.set);
        Set<CameraPixel> cameraPixels = union.immutableCopy();

        PixelSetOverlay outset = new PixelSetOverlay(cameraPixels);

        input.put(outsetKey, outset);

        return input;
    }
}
