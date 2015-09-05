package fact.auxservice.strategies;

import fact.auxservice.AuxPoint;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.TreeSet;

/**
 * Returns the closest (in time)  AuxPoint found in the TreeSet that is not null.
 * Created by kai on 01.04.15.
 */
public class Closest implements AuxPointStrategy {
    /**
     * Returns the closest (in time)  AuxPoint found in the TreeSet that is not null.
     * @param set the set from which to get the points
     * @param eventTimeStamp the timestamp for which you want the auxiliary data
     * @return an AuxPoint according to the concrete strategy implementation.
     */
    @Override
    public AuxPoint getPointFromTreeSet(TreeSet<AuxPoint> set, DateTime eventTimeStamp) {
        AuxPoint dummyPoint = new AuxPoint(eventTimeStamp);
        AuxPoint floorPoint = set.floor(dummyPoint);
        AuxPoint ceilPoint = set.ceiling(dummyPoint);

        AuxPoint retVal = null;

        if (floorPoint != null && ceilPoint != null ) {
            Duration durationFloor = new Duration(floorPoint.getTimeStamp(), eventTimeStamp);
            Duration durationCeil = new Duration(eventTimeStamp,  ceilPoint.getTimeStamp());

            if (durationCeil.isShorterThan(durationFloor)){
                retVal=  ceilPoint;
            } else {
                retVal=  floorPoint;
            }
        } else if (ceilPoint != null){
            retVal=  ceilPoint;
        } else if (floorPoint != null){
            retVal=  floorPoint;
        }
        return retVal;
    }
}
