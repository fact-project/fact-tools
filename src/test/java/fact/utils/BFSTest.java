package fact.utils;

import fact.Utils;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by bruegge on 7/30/14.
 */
public class BFSTest {
    Integer[] pixels = {0,3,6,17,2,5,29,27,25,  200,   156,154,153};
    Integer[] pixelsCluster1 = {0,3,6,17,2,5,29,27,25};
    Integer[] pixelsCluster2 = {156,154,153};
    ArrayList<Integer> mock;
    ArrayList<Integer> cluster1;
    ArrayList<Integer> cluster2;

    @Before
    public void createMockList(){
        mock = new ArrayList<>(Arrays.asList(pixels));
        cluster1 = new ArrayList<>(Arrays.asList(pixelsCluster1));
        cluster2 = new ArrayList<>(Arrays.asList(pixelsCluster2));
    }

    @Test
    public void testBFS(){
        ArrayList<ArrayList<Integer>> a =  Utils.breadthFirstSearch(mock);
        for (ArrayList<Integer> cluster : a) {
            if(cluster.size() == 9){
                assertTrue("This cluster should contain the pixel chids {0,3,6,17,2,5,29,27,25}", cluster.containsAll(cluster1));
                continue;
            }
            else if (cluster.size() == 1){
                assertTrue("Cluster should have the pixel 200", cluster.contains(200));
                continue;
            }
            else if (cluster.size()==3){
                assertTrue("This cluster should contain the pixel chids {156,154,153};", cluster.containsAll(cluster2));
                continue;
            }
            else {
                fail("One cluster had the wrong size: " + cluster.size());
            }

        }
    }

}
