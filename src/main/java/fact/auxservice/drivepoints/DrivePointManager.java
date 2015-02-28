package fact.auxservice.drivepoints;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;

import java.util.ArrayList;
import java.util.Collections;


/**
 *
 * The DrivePointManager provides an abstraction to the auxiliary .fits files needed for analysing FACT data.
 * Aux files contain rows of data with a timestamp. Given the timestamp of the currently analysed event this returns
 * the data from the file as a DrivePoint container.
 * This expects the data in the aux files to have ascending timestamps.
 *
 * In case the timestamps are not sorted the Manager will throw an error.
 *
 * All times are given as julian day
 *
 * Created by kaibrugge on 06.10.14.
 */
public class DrivePointManager<T extends DrivePoint>{
    static Logger log = LoggerFactory.getLogger(DrivePointManager.class);

    //This list will be populated with DrivePoints
    private ArrayList<T> locList = new ArrayList<>();

    public void addDrivePoint(T p){
        //get last timestamp
        int end = locList.size();
        if (end > 0 && locList.get(end-1).getTime() > p.getTime() ){
            log.error("Timestamps in auxiliary file are not ordered. The file might be broken. Aborting process.");
            throw new RuntimeException("Aux file is broken.");
        }

        locList.add(p);
    }

    /**
     * Returns the data from the auxiliary file for the given timestamp.
     * @param currentTime timestamp of the current event as julian day
     * @return the point from the aux file with the closest timestamp below the given currentTime
     */
    public T getPoint(double currentTime) {
        if (locList.isEmpty()){
            log.error("No points from auxilary data in DrivePointManager ");
        }
        int index = Collections.binarySearch(locList, currentTime);

        //element was found
        if (index >= 0){
            return locList.get(index);
        } else {
            int insertionPoint = - ( index + 1 );
            if(insertionPoint == 0){

                //calculate distance to first drivepoint in list if it exists
                double dist = locList.get(0).getTime() - currentTime;
                log.warn("Returning the first item in the auxiliary file. Your timestamps might be wrong. " +
                            "Distance in julianDay: " + dist);
                return locList.get(0);
            }
            if (insertionPoint >= locList.size()){
//                    System.out.println("returnning last in list");
                double dist = locList.get(locList.size()-1).getTime() - currentTime;
                log.warn("EventTime larger than last point in File. Distance in julianDay: " + dist);
                return locList.get(locList.size()-1);
            }

            T lower = locList.get(insertionPoint-1);
            return lower;
        }
    }
}