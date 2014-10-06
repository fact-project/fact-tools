package fact.features.source.drivepoints;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Created by kaibrugge on 06.10.14.
 * TODO: some sort of sanity check for the data points we add.
 */
public class DrivePointManager<T extends DrivePoint>{
    static Logger log = LoggerFactory.getLogger(DrivePointManager.class);

    //position of the Telescope
    public final double mLongitude                  = -17.890701389;
    public final double mLatitude                   = 28.761795;
    //Distance from earth center
    public final double mDistance                   = 4890.0;

    //This list will be populated with DrivePoints
    private ArrayList<T> locList = new ArrayList<>();

    public void addTrackingPoint(T p){
        locList.add(p);
    }

    public T getPoint(double currentTime) {

        int index = Collections.binarySearch(locList, currentTime);

        //element was found
        if (index >= 0){
            return locList.get(index);
        } else {
            int insertionPoint = - ( index + 1 );
            if(insertionPoint == 0){
//                    System.out.println("Returning first in list");
                return locList.get(0);
            }
            if (insertionPoint >= locList.size()){
//                    System.out.println("returnning last in list");
                log.warn("EventTime larger than last point in Tracking File");
                return locList.get(locList.size()-1);
            }

            T lower = locList.get(insertionPoint-1);
            T higher = locList.get(insertionPoint);
            if ( lower.distanceTo(currentTime) < higher.distanceTo(currentTime) ){
//                    System.out.println("returning lower");
                return lower;
            } else {
//                    System.out.println("returning highrer");
                return higher;
            }

        }
    }
}