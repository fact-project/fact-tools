package fact.pixelsets;

import com.google.common.collect.Sets;
import fact.Utils;
import fact.hexmap.CameraPixel;
import fact.container.PixelSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.Set;


/**
 * This processor gets a pixel set and returns the reverted set for the whole camera.
 * Created by jebuss on 17.12.15.
 */
public class Invert implements Processor{
    static Logger log = LoggerFactory.getLogger(Invert.class);

    @Parameter(required = true, description = "key to the first set to be united")
    private String insetKey;

    @Parameter(required = true, description = "key to the output set which contains the inversion")
    private String outsetKey;

    @Override
    public Data process(Data input) {

        if (!input.containsKey(insetKey)) {
            return input;
        }
        Utils.isKeyValid(input, insetKey, PixelSet.class);
        PixelSet inset = (PixelSet) input.get(insetKey);

        int npix = (Integer) input.get("NPIX");

        PixelSet wholeCamSet = createFullCameraSet(npix);

        try {
            Sets.SetView<CameraPixel> inversion = Sets.difference(wholeCamSet.set, inset.set);
            Set<CameraPixel> cameraPixels = inversion.immutableCopy();

            PixelSet outset = new PixelSet(cameraPixels);

            input.put(outsetKey, outset);
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        return input;
    }

    public PixelSet createFullCameraSet(int npix) {
        PixelSet wholeCamSet = new PixelSet();
        for (int pix = 0; pix < npix; pix++) {
            wholeCamSet.addById(pix);
        }
        return wholeCamSet;
    }

    public void setInsetKey(String insetKey) {
        this.insetKey = insetKey;
    }

    public void setOutsetKey(String outsetKey) {
        this.outsetKey = outsetKey;
    }
}
