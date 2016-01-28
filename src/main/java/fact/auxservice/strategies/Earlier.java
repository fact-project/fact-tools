package fact.auxservice.strategies;

import fact.auxservice.AuxPoint;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.TreeSet;

/**
 * Returns the AuxPoint which is earlier or equal to the given time stamp if it exists.
 * Created by kai on 01.04.15.
 */
public class Earlier implements AuxPointStrategy {

    /**
     * Returns the AuxPoint which is earlier or equal to the given time stamp if it exists.
     * @param set the set from which to get the points
     * @param eventTimeStamp the timestamp for which you want the auxiliary data
     * @return an AuxPoint according to the concrete strategy implementation or null if it doesn't exist.
     */
    @Override
    public AuxPoint getPointFromTreeSet(TreeSet<AuxPoint> set, DateTime eventTimeStamp) {
        AuxPoint dummyPoint = new AuxPoint(eventTimeStamp);
        return set.floor(dummyPoint);
    }
}
