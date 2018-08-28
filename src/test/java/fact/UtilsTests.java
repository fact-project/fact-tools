package fact;

import org.junit.Test;
import stream.Data;
import stream.data.DataFactory;

import static org.junit.Assert.*;

/**
 * Created by jebuss on 14.11.16.
 */
public class UtilsTests {
    @Test
    public void flattenEmpty2dArray() {

        double[][] empty = new double[0][0];
        double[] result = Utils.flatten2dArray(empty);
        assertEquals(result.length, 0);
    }

    @Test
    public void testTransformToEllipseCoordinates() {

        double x = 10;
        double y = 0;
        double delta = 0;
        double cogX = 20;
        double cogY = 0;

        double[] coords = Utils.transformToEllipseCoordinates(x, y, cogX, cogY, delta);

        assertEquals(coords[0], -10, 1e-12);
        assertEquals(coords[1], 0, 1e-12);

        x = 10;
        y = 10;
        cogX = 0;
        cogY = 10;
        delta = Math.PI / 2;

        coords = Utils.transformToEllipseCoordinates(x, y, cogX, cogY, delta);
        assertEquals(coords[0], 0, 1e-12);
        assertEquals(coords[1], -10, 1e-12);

        x = -10;
        y = 10;
        cogX = 0;
        cogY = 10;
        delta = Math.PI / 2;

        coords = Utils.transformToEllipseCoordinates(x, y, cogX, cogY, delta);
        assertEquals(coords[0], 0, 1e-12);
        assertEquals(coords[1], 10, 1e-12);
    }

    @Test
    public void testMapContainsKeys() {
        Data item = DataFactory.create();
        item.put("stuff", 4);
        item.put("other", 2);
        try {
            Utils.mapContainsKeys(item, "stuff", "other", "bla");
            fail("mapContainsKeys should have thrown because of missing key bla");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("bla"));
            assertTrue(e.getMessage().contains("fact.UtilsTests"));
        }
    }
}
