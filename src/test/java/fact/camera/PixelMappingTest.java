package fact.camera;

import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.*;


public class PixelMappingTest {

    @Test
    public void testFactMapping() {
        FactPixelMapping m = FactPixelMapping.getInstance();

        //get pixel with chid 969
        CameraPixel p = m.getPixelFromId(969);
        assertTrue("Wrong id in pixel for chid 969", p.id == 969);

        //get pixel with chid 969
        p = m.getPixelFromId(1269);
        assertTrue("Wrong id in pixel for chid 1269", p.id == 1269);

        //get pixel with chid 969
        p = m.getPixelFromId(9);
        assertTrue("Wrong id in pixel for chid 9", p.id == 9);
    }


    @Test
    public void testAxialCoordinateMapping() {
        FactPixelMapping m = FactPixelMapping.getInstance();

        //check existing pixel
        CameraPixel p = m.getPixelFromOffsetCoordinates(0, 0);
        assertTrue("Pixel didnt have the desired coordinates", p.geometricX == 0 && p.geometricY == 0);

        p = m.getPixelFromOffsetCoordinates(-8, 19);
        assertTrue("Pixel didnt have the desired coordinates", p.geometricX == -8 && p.geometricY == 19);

        p = m.getPixelFromOffsetCoordinates(14, 16);
        assertTrue("Pixel didnt have the desired coordinates", p.geometricX == 14 && p.geometricY == 16);

        p = m.getPixelFromOffsetCoordinates(22, -5);
        assertTrue("Pixel didnt have the desired coordinates", p.geometricX == 22 && p.geometricY == -5);

        //check not existing pixels

        p = m.getPixelFromOffsetCoordinates(22, -6);
        assertTrue("Pixel should be null for offset 22,-6", p == null);

        p = m.getPixelFromOffsetCoordinates(122, -126);
        assertTrue("Pixel should be null for offset 122,-126", p == null);
    }

    @Test
    public void testNeighbourFinding() {
        FactPixelMapping m = FactPixelMapping.getInstance();

        //get neighbours for chid 393
        //expect softids: 40,41,65,97,98,67
        int chid = 393;
        int[] ne1 = {390, 391, 394, 1098, 395, 392};

        CameraPixel[] n = m.getNeighborsFromID(chid);
        assertTrue("Neighbour list too short for chid " + chid + ". Expect 6 neighbours", n.length == 6);
        assertTrue("Neighbour array does not contain the right pixels", pixelArrayContainsChids(n, ne1));

        chid = 1060;
        n = m.getNeighborsFromID(chid);
        int[] ne2 = {1058, 1055, 1056};
        assertTrue("Neighbour list has wrong size for chid " + chid + ". Expect 3 neighbours", n.length == 3);
        assertTrue("Neighbour array does not contain the right pixels", pixelArrayContainsChids(n, ne2));

        chid = 13;
        n = m.getNeighborsFromID(chid);
        int[] ne3 = {10, 12, 15, 16};
        assertTrue("Neighbour list has wrong size for chid " + chid + ". Expect 4 neighbours", n.length == 4);
        assertTrue("Neighbour array does not contain the right pixels", pixelArrayContainsChids(n, ne3));

        chid = 70;
        n = m.getNeighborsFromID(chid);
        int[] ne4 = {7, 71, 68};
        assertTrue("Neighbour list has wrong size for chid " + chid + ". Expect 3 neighbours", n.length == 3);
        assertTrue("Neighbour array does not contain the right pixels", pixelArrayContainsChids(n, ne4));

        chid = 49;
        n = m.getNeighborsFromID(chid);
        int[] ne5 = {51, 50, 46};
        assertTrue("Neighbour list has wrong size for chid " + chid + ". Expect 3 neighbours", n.length == 3);
        assertTrue("Neighbour array does not contain the right pixels", pixelArrayContainsChids(n, ne5));

        chid = 45;
        n = m.getNeighborsFromID(chid);
        int[] ne6 = {46, 47, 89, 87, 86};
        assertTrue("Neighbour list has wrong size for chid " + chid + ". Expect 5 neighbours", n.length == 5);
        assertTrue("Neighbour array does not contain the right pixels", pixelArrayContainsChids(n, ne6));

        chid = 1160;
        n = m.getNeighborsFromID(chid);
        int[] ne7 = {1159, 1157, 1051};
        assertTrue("Neighbour list has wrong size for chid " + chid + ". Expect 3 neighbours", n.length == 3);
        assertTrue("Neighbour array does not contain the right pixels", pixelArrayContainsChids(n, ne7));

        chid = 859;
        n = m.getNeighborsFromID(chid);
        int[] ne8 = {856, 858, 861, 862, 749, 683};
        assertTrue("Neighbour list has wrong size for chid " + chid + ". Expect 6 neighbours", n.length == 6);
        assertTrue("Neighbour array does not contain the right pixels", pixelArrayContainsChids(n, ne8));

    }


    /**
     * Helper Function to check if the Pixel array contains the right chids
     *
     * @param ar    the array of CameraPixel
     * @param chids
     * @return if the pixel array contains the correct chids
     */
    private boolean pixelArrayContainsChids(CameraPixel[] ar, int[] chids) {
        for (CameraPixel p : ar) {
            int id = p.chid;
            return arrayContains(chids, id);
        }
        return true;
    }

    private boolean arrayContains(int[] ar, int id) {
        for (int v : ar) {
            if (v == id) {
                return true;
            }
        }
        return false;
    }


    @Test
    public void testSoftIdToChid() {
        FactPixelMapping m = FactPixelMapping.getInstance();

        m.getNumberOfPixel();
        int softid = 1410;
        int chid = 1423;
        assertTrue("SoftId " + softid + " should map to " + chid + " but was: " + m.getChidFromSoftID(softid),
                m.getChidFromSoftID(softid) == chid);


        softid = 1217;
        chid = 1060;
        assertTrue("SoftId " + softid + " should map to " + chid + " but was: " + m.getChidFromSoftID(softid),
                m.getChidFromSoftID(softid) == chid);


        softid = 765;
        chid = 844;
        assertTrue("SoftId " + softid + " should map to " + chid + " but was: " + m.getChidFromSoftID(softid),
                m.getChidFromSoftID(softid) == chid);


        softid = 1390;
        chid = 731;
        assertTrue("SoftId " + softid + " should map to " + chid + " but was: " + m.getChidFromSoftID(softid),
                m.getChidFromSoftID(softid) == chid);
    }


    /**
     * writes a file called 'matrix.txt' which can be plotted by matplotlib like this:
     * >>>  a = np.loadtxt("matrix.txt")
     * >>>  plt.imshow(b, interpolation='nearest', aspect='auto')
     * >>>  plt.show()
     */
    public void imageTest() {
        FactPixelMapping m = FactPixelMapping.getInstance();
        double step = 0.6;
        File output = new File("matrix.txt");
        try {
            FileWriter w = new FileWriter(output);
            //BufferedWriter writer = new BufferedWriter(new Writer)
            //walk through rows
            for (double y = -220; y < 220; y += step) {
                for (double x = -280; x < 280; x += step) {
                    CameraPixel p = m.getPixelBelowCoordinatesInMM(x, y);
                    if (p != null) {
                        int id = p.id;
                        w.write(String.valueOf(id));
                        w.write(" ");
                    } else {
                        w.write("-220");
                        w.write(" ");
                    }
                }
                w.write("\n");
            }
            w.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Tests the mapping of camera coordinates in millimeter to actual pixels.
     * Uses some hardcoded coordinates that can be looked up on the poster.
     */
    @Test
    public void testCoordinatesInMMToPixel() {
        FactPixelMapping m = FactPixelMapping.getInstance();

        double x = -0.5 * 9.5;
        double y = 0.0 * 9.5;
        int chid = 393;
        CameraPixel p = m.getPixelBelowCoordinatesInMM(x, y);
        assertTrue("Map didnt return the right pixel " + chid + " for coordinates: " + x + ", " + y,
                chid == p.chid);

        //pixel in lower left corner
        x = -5.5f * 9.5f;
        y = -19.05f * 9.5f;
        chid = 722;
        p = (CameraPixel) m.getPixelFromId(722);
        p = m.getPixelBelowCoordinatesInMM(x, y);
        assertTrue("Map didnt return the right pixel " + chid + " for coordinates: " + x + ", " + y + "\n" +
                " returnded chid was: " + p.chid + " with coordinates: "
                + p.getXPositionInMM() + ", " + p.getYPositionInMM(), chid == p.chid);


        //still the same pixel
        x = -5.6f * 9.5f;
        y = -19.06f * 9.5f;
        chid = 722;
        assertTrue("Map didnt return the right pixel " + chid + " for coordinates: " + x + ", " + y,
                chid == m.getPixelBelowCoordinatesInMM(x, y).chid);

        //outside camera bounds
        x = -30.06f * 9.5f;
        y = 9.6f * 9.5f;
        assertTrue("Map should return null for non existing pixel for coordinates: " + x + ", " + y,
                m.getPixelBelowCoordinatesInMM(x, y) == null);

    }

    /**
     * Similar to the test above. Just moar. And moar is better!
     * Tests the mapping of camera coordinates in millimeter to actual pixels.
     * We start with some handcrafted coordinates in MM. For each coordinate we find the nearest pixel by
     * iterating over all pixels and calculating the distance
     */
    @Test
    public void testCoordinateToChid() {
        FactPixelMapping m = FactPixelMapping.getInstance();

        // -180,999 .... 180,999
        double[] xs = {-80.113, -102.22, 5.324, -20.5, -3.49 * 9.5, -100.4, -11 * 9.5, -6.0 * 9.5};
        double[] ys = {120.513, 12.22, -80.324, -120.6, -6.93 * 9.5, 100.0, 10.70 * 9.5, -11.26 * 9.5};

        for (int i = 0; i < xs.length; i++) {
            double x = xs[i];
            double y = ys[i];

            int nearestChid = -1;
            double lowestDistance = 100000.0d;

            for (int chid = 0; chid < m.getNumberOfPixel(); chid++) {
                CameraPixel p = m.getPixelFromId(chid);

                double xChid = p.getXPositionInMM();
                double yChid = p.getYPositionInMM();

                double distance = Math.sqrt(Math.pow(xChid - x, 2) + Math.pow(yChid - y, 2));

                if (distance <= lowestDistance) {
                    nearestChid = chid;
                    lowestDistance = distance;
                }
            }
            CameraPixel pixel = m.getPixelBelowCoordinatesInMM(x, y);
            if (pixel != null) {
                assertEquals("Map did not return the right pixel for coordinates: " + x + ", " + y + " (" + i + ")",
                         nearestChid, pixel.chid);
            } else {
                fail("No pixel returned for coordinates: " + x + ", " + y);
            }
        }
    }

}
