package fact.camera;

import fact.hexmap.FactPixelMapping;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

/**
 * Created by lena on 03.12.15.
 */
public class CubeCoordinateTest {
    FactPixelMapping mapping = FactPixelMapping.getInstance();


    @Test
    public void testLine() {
        ArrayList<Integer> line1 = mapping.line(1128, 378);
        ArrayList<Integer> line2 = mapping.line(295, 574);
        assertTrue("Wrong line pixel " + line1.get(1) + ". Should be id 1126.", line1.get(1) == 1126);
        assertTrue("Wrong line pixel " + line1.get(2) + ". Should be id 1114.", line1.get(2) == 1114);
        assertTrue("Wrong line pixel " + line1.get(3) + ". Should be id 1100.", line1.get(3) == 1100);
        assertTrue("Wrong line pixel " + line1.get(7) + ". Should be id 388.", line1.get(7) == 388);
        assertTrue("Wrong line pixel " + line1.get(8) + ". Should be id 383.", line1.get(8) == 383);
        assertTrue("Wrong line pixel " + line1.get(9) + ". Should be id 380.", line1.get(9) == 380);

        assertTrue("Wrong line pixel " + line2.get(1) + ". Should be id 329.", line2.get(1) == 329);
        assertTrue("Wrong line pixel " + line2.get(2) + ". Should be id 330.", line2.get(2) == 330);
        assertTrue("Wrong line pixel " + line2.get(3) + ". Should be id 342.", line2.get(3) == 342);
        assertTrue("Wrong line pixel " + line2.get(7) + ". Should be id 460.", line2.get(7) == 460);
        assertTrue("Wrong line pixel " + line2.get(8) + ". Should be id 463.", line2.get(8) == 463);
        assertTrue("Wrong line pixel " + line2.get(9) + ". Should be id 497.", line2.get(9) == 497);
    }


}
