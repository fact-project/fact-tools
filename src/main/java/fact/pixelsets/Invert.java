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
 * This processor gets a pixel set and returns the reverted set for the whole camera.
 * Created by jebuss on 17.12.15.
 */
public class Invert implements Processor {
    static Logger log = LoggerFactory.getLogger(Invert.class);

    @Parameter(required = true, description = "key to the set to be inverted")
    public String insetKey;

    @Parameter(required = true, description = "key to the output set which contains the inversion")
    public String outsetKey;

    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, insetKey, PixelSet.class);
        PixelSet inset = (PixelSet) item.get(insetKey);

        int npix = (Integer) item.get("NPIX");

        PixelSet wholeCamSet = createFullCameraSet(npix);

        Sets.SetView<CameraPixel> inversion = Sets.difference(wholeCamSet.set, inset.set);

        PixelSet outset = new PixelSet();
        inversion.copyInto(outset.set);
        item.put(outsetKey, outset);

        return item;
    }

    public PixelSet createFullCameraSet(int npix) {
        PixelSet wholeCamSet = new PixelSet();
        for (int pix = 0; pix < npix; pix++) {
            wholeCamSet.addById(pix);
        }
        return wholeCamSet;
    }
}
