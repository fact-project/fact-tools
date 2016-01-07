package fact;

import fact.container.PixelSetOverlay;
import fact.pixelsets.Invert;
import org.junit.Test;
import stream.Data;
import stream.data.DataFactory;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by jebuss on 04.01.16.
 */
public class PixelSetTest {

    @Test
    public void createFullCameraSetTest(){
        Invert i = new Invert();
        PixelSetOverlay fullCameraSet = i.createFullCameraSet(1440);
        assertThat(fullCameraSet.set.size(), is(1440));
    }

    @Test
    public void invertTest(){
        Data item = DataFactory.create();

        PixelSetOverlay testSet = new PixelSetOverlay();
        for (int pix = 0; pix < 20; pix++) {
            testSet.addById(pix);
        }
        item.put("testSet", testSet );
        item.put("NPIX", 1440);
        Invert i = new Invert();

        i.setInsetKey("testSet");
        i.setOutsetKey("testInversion");
        Data test = i.process(item);
        assertThat(test.containsKey("testInversion"), is(true));


        PixelSetOverlay inversion = (PixelSetOverlay) test.get("testInversion");
        assertThat(inversion.set.size(), is(1440-20));
    }
}
