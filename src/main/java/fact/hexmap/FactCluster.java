package fact.hexmap;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by lena on 12.11.15.
 */
public class FactCluster {
    //public int[] contentPixel;
    ArrayList<Integer> contentPixel = new ArrayList<>();
    //public int[] boundPixel;
    ArrayList<Integer> boundPixel = new ArrayList<>();
    ArrayList<Double> contentPixelPhotoncharge = new ArrayList<>();
    ArrayList<Double> contentPixelArrivaltime = new ArrayList<>();

/*    ArrayList<Double> boundPixelPhotoncharge = new ArrayList<>();
    ArrayList<Double> boundPixelArrivaltime = new ArrayList<>();*/

    public int numPixel;
    public int cogId;
    public int photonchargeMaxId;
    public boolean containsCleaningPixel;


    public void addContentPixel(int id){
        contentPixel.add(id);
    }

    public void addBoundPixel(int id){
        boundPixel.add(id);
    }

    public int getNumPixel(){
        return numPixel;
    }

    public int getCogId() {
        return cogId;
    }

    public int getPhotonchargeMaxId(){
        return photonchargeMaxId;
    }

    public double maxPhotoncharge(){
        return Collections.max(contentPixelPhotoncharge);
    }

    public int maxPhotonchargeID(){
        return contentPixelPhotoncharge.indexOf(maxPhotoncharge());
    }


}
