package fact.coordinates;

import fact.Constants;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by maxnoe on 23.05.17.
 */
public class CameraCoordinateTest {

    @Test
    public void euclideanDistanceTest() {
        CameraCoordinate c1 = new CameraCoordinate(-1, -1);
        CameraCoordinate c2 = new CameraCoordinate(1, 1);
        assertEquals(Math.sqrt(8), c1.euclideanDistance(c2), 1e-12);

        c1 = new CameraCoordinate(5, 0);
        c2 = new CameraCoordinate(0, 0);
        assertEquals(5, c1.euclideanDistance(c2), 1e-12);

        c1 = new CameraCoordinate(0, 5);
        c2 = new CameraCoordinate(0, 0);
        assertEquals(5, c1.euclideanDistance(c2), 1e-12);

    }

    @Test
    public void testToHorizontal() {
        CameraCoordinate c;
        HorizontalCoordinate h;
        HorizontalCoordinate pointing = HorizontalCoordinate.fromDegrees(90, 90);

        c = new CameraCoordinate(0.0, 0.0);
        h = c.toHorizontal(pointing, Constants.FOCAL_LENGTH_MM);

        assertEquals("Azimuth for CameraCoordinate(0,0) wrong", 90.0, h.getAzimuthDeg(), 1e-12);
        assertEquals("Zenith for CameraCoordinate(0,0) wrong", 90.0, h.getZenithDeg(), 1e-12);

        pointing = HorizontalCoordinate.fromDegrees(90, 0);
        c = new CameraCoordinate(0, 50.0);
        h = c.toHorizontal(pointing, Constants.FOCAL_LENGTH_MM);

        assertTrue(h.getZenithDeg() > 90);
        assertEquals(0.0, h.getAzimuthDeg(), 1e-12);

        c = new CameraCoordinate(-50, 0);
        h = c.toHorizontal(pointing, Constants.FOCAL_LENGTH_MM);

        assertEquals(90.0, h.getZenithDeg(), 1e-12);
        assertTrue(h.getAzimuthDeg() > 0);

    }
}
