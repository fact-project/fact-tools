package fact.camera;

import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by lena on 14.08.15.
 */
public class TestNeighbour {
    @Test
    public void testNeighbourFinding() {
        FactPixelMapping m = FactPixelMapping.getInstance();

        //get neighbours for chid 393
        //expect softids: 40,41,65,97,98,67
        int chid = 284;
        int[] ne1 = {283,286,1107,1147,1109,1146,1143,258,256,278,279,280};

        FactCameraPixel[] n = m.getSecondOrderNeighboursFromID(chid);
        for(int i=0; i<n.length; i++){
            System.out.println(n[i]);
        }
        System.out.println();
        assertTrue("Neighbour list too short for chid " + chid + ". Expect 12 neighbours", n.length == 12);
        assertTrue("Neighbour array does not contain the right pixels", pixelArrayContainsChids(n, ne1));

        chid = 70;
        n = m.getSecondOrderNeighboursFromID(chid);
        for(int i=0; i<n.length; i++){
            System.out.println(n[i]);
        }
        System.out.println();
        int[] ne2 = {4, 6, 8, 69, 67, 66};
        assertTrue("Neighbour list has wrong size for chid " + chid + ". Expect 6 neighbours", n.length == 6);
        assertTrue("Neighbour array does not contain the right pixels", pixelArrayContainsChids(n, ne2));

        chid = 780;
        n = m.getSecondOrderNeighboursFromID(chid);
        int[] ne3 = {766,763,760,757,777,776,789,790};
        for(int i=0; i<n.length; i++){
            System.out.println(n[i]);
        }
        System.out.println();
        assertTrue("Neighbour list has wrong size for chid " + chid + ". Expect 8 neighbours", n.length == 8);
        assertTrue("Neighbour array does not contain the right pixels", pixelArrayContainsChids(n, ne3));

        chid = 1339;
        n = m.getSecondOrderNeighboursFromID(chid);
        int[] ne4 = {1303,1304,1302,1299,1296,1311,1310,1348,1336,1334,1340};
        for(int i=0; i<n.length; i++){
            System.out.println(n[i]);
        }
        System.out.println();
        assertTrue("Neighbour list has wrong size for chid " + chid + ". Expect 8 neighbours", n.length == 11);
        assertTrue("Neighbour array does not contain the right pixels", pixelArrayContainsChids(n, ne4));
    }

    private boolean pixelArrayContainsChids(FactCameraPixel[] ar,  int[] chids){
        for (FactCameraPixel p : ar ) {
            int id = p.chid;
            return arrayContains(chids, id);
        }
        return true;
    }

    private boolean arrayContains(int[] ar, int id){
        for (int v : ar){
            if (v == id){
                return true;
            }
        }
        return false;
    }
}
