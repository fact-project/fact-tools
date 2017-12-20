package fact.utils;

import fact.Utils;
import fact.container.PixelSet;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;

/**
 * Created by bruegge on 7/30/14.
 */
public class BFSTest {
    int[] pixels = {0, 3, 6, 17, 2, 5, 29, 27, 25, 200, 156, 154, 153};
    int[] pixelsCluster1 = {0, 3, 6, 17, 2, 5, 29, 27, 25};
    int[] pixelsCluster2 = {156, 154, 153};
    PixelSet mock;
    PixelSet cluster1;
    PixelSet cluster2;

    @Before
    public void createMockList() {
        mock = PixelSet.fromIDs(pixels);
        cluster1 = PixelSet.fromIDs(pixelsCluster1);
        cluster2 = PixelSet.fromIDs(pixelsCluster2);
    }

    @Test
    public void testBFS() {
        ArrayList<PixelSet> a = Utils.breadthFirstSearch(mock);
        for (PixelSet cluster : a) {
            if (cluster.size() == 9) {
                assertTrue("This cluster should contain the pixel chids {0,3,6,17,2,5,29,27,25}", cluster.containsAll(cluster1));
                continue;
            } else if (cluster.size() == 1) {
                assertTrue("Cluster should have the pixel 200", cluster.containsID(200));
                continue;
            } else if (cluster.size() == 3) {
                assertTrue("This cluster should contain the pixel chids {156,154,153};", cluster.containsAll(cluster2));
                continue;
            } else {
                fail("One cluster had the wrong size: " + cluster.size());
            }

        }
    }

}
