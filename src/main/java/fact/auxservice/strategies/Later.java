package fact.auxservice.strategies;

import fact.auxservice.AuxPoint;
import org.joda.time.DateTime;

import java.util.TreeSet;

/**
 * Returns the AuxPoint which is later or equal to the given time stamp if it exists.
 * Created by kai on 01.04.15.
 */
public class Later implements AuxPointStrategy {

    /**
     * Returns the AuxPoint which is later or equal to the given time stamp if it exists.
     * @param set the set from which to get the points
     * @param eventTimeStamp the timestamp for which you want the auxiliary data
     * @return an AuxPoint according to the concrete strategy implementation.
     */
    @Override
    public AuxPoint getPointFromTreeSet(TreeSet<AuxPoint> set, DateTime eventTimeStamp) {
        AuxPoint dummyPoint = new AuxPoint(eventTimeStamp);
        return set.ceiling(dummyPoint);
    }
}
