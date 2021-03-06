package fact.container;

import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by maxnoe on 21.11.17.
 */
public class PixelSetTest {

    FactPixelMapping pixelMapping = FactPixelMapping.getInstance();

    @Test
    public void testContains() {
        PixelSet pixelSet = new PixelSet();
        PixelSet pixelSet2 = new PixelSet();
        CameraPixel pixel1 = pixelMapping.getPixelFromId(1);
        CameraPixel pixel100 = pixelMapping.getPixelFromId(100);

        assertFalse(pixelSet.contains(pixel1));
        assertFalse(pixelSet.contains(pixel100));
        assertFalse(pixelSet.containsCHID(pixel1.id));
        assertFalse(pixelSet.containsCHID(pixel100.id));

        pixelSet.add(pixel1);

        assertTrue(pixelSet.contains(pixel1));
        assertFalse(pixelSet.contains(pixel100));
        assertTrue(pixelSet.containsCHID(pixel1.id));
        assertFalse(pixelSet.containsCHID(pixel100.id));

        pixelSet2.add(pixel1);
        pixelSet2.add(pixel100);
        assertFalse(pixelSet.containsAll(pixelSet2));
        assertFalse(pixelSet.containsAllCHIDs(new int[]{1, 100}));

        pixelSet.add(pixel100);
        assertTrue(pixelSet.containsAll(pixelSet2));
        assertTrue(pixelSet.containsAllCHIDs(new int[]{1, 100}));
    }

    @Test
    public void testToBooleanArray() {

        PixelSet pixelSet = new PixelSet();
        pixelSet.addByCHID(2);
        pixelSet.addByCHID(3);
        pixelSet.addByCHID(5);
        pixelSet.addByCHID(7);
        pixelSet.addByCHID(511);

        boolean[] mask = pixelSet.toBooleanArray();
        assertTrue(mask[2]);
        assertTrue(mask[3]);
        assertTrue(mask[5]);
        assertTrue(mask[7]);
        assertTrue(mask[511]);
        assertFalse(mask[510]);
        assertFalse(mask[1]);
        assertFalse(mask[0]);
    }

}
