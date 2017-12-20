package fact.auxservice.strategies;

import fact.auxservice.AuxPoint;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.TreeSet;

/**
 * Returns the closest (in time)  AuxPoint found in the TreeSet that is not null.
 * Created by kai on 01.04.15.
 */
public class Closest implements AuxPointStrategy {
    /**
     * Returns the closest (in time)  AuxPoint found in the TreeSet that is not null.
     *
     * @param set            the set from which to get the points
     * @param eventTimeStamp the timestamp for which you want the auxiliary data
     * @return an AuxPoint according to the concrete strategy implementation, or null if it doesn't exist.
     */
    @Override
    public AuxPoint getPointFromTreeSet(TreeSet<AuxPoint> set, ZonedDateTime eventTimeStamp) {
        AuxPoint dummyPoint = new AuxPoint(eventTimeStamp);
        AuxPoint floorPoint = set.floor(dummyPoint);
        AuxPoint ceilPoint = set.ceiling(dummyPoint);

        AuxPoint retVal = null;

        if (floorPoint != null && ceilPoint != null) {
            Duration durationFloor = Duration.between(floorPoint.getTimeStamp(), eventTimeStamp);
            Duration durationCeil = Duration.between(eventTimeStamp, ceilPoint.getTimeStamp());

            //Duration durationFloor = new Duration(floorPoint.getTimeStamp(), eventTimeStamp);
            //Duration durationCeil = new Duration(eventTimeStamp,  ceilPoint.getTimeStamp());
            if ((durationCeil.compareTo(durationFloor)) < 0) {
                retVal = ceilPoint;
            } else {
                retVal = floorPoint;
            }
        } else if (ceilPoint != null) {
            retVal = ceilPoint;
        } else if (floorPoint != null) {
            retVal = floorPoint;
        }
        return retVal;
    }
}
