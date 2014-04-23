package fact.features;

import fact.viewer.ui.DefaultPixelMapping;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LeakageTest {

    @Test
    public void borderPixelFunction() {
        int[] pixelIds = {70,
                16,
                944,
                768,
                686,
                433,
                1196};
        Leakage l = new Leakage();
        for (int pix: pixelIds){
            assertTrue("Pixel with chid " + pix + " should be recognized as a borderpixel.",
                    l.isBorderPixel(pix));
        }
    }
}
