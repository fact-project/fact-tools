package fact.features;

import fact.container.PixelSet;
import fact.hexmap.CameraPixel;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LeakageTest {

    @Test
    public void borderPixelFunction() {
        int[] pixelIds = {70, 16, 944, 768, 686, 433, 1196};
        PixelSet pixelSet = PixelSet.fromIDs(pixelIds);
        Leakage l = new Leakage();
        for (CameraPixel pixel : pixelSet) {
            assertTrue("Pixel with chid " + pixel.id + " should be recognized as a borderpixel.",
                    l.isBorderPixel(pixel));
        }
    }
}
