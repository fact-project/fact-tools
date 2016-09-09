package fact.auxservice.strategies;

import fact.auxservice.AuxPoint;
import org.joda.time.DateTime;

import java.util.TreeSet;

/**
 * An AuxPointStrategy defines which point from a given TreeSet should be returned.
 * You could also interpolate between two results and return a new interpolated Point.
 * This can return null if the data doesnt exist in this context. For example if the File from which the data
 * was read does not contain a valid point for that eventTimestamp
 * Created by kai on 01.04.15.
 */
public interface AuxPointStrategy {
    /**
     *
     * @param set the set from which to get the points
     * @param eventTimeStamp the timestamp for which you want the auxiliary data
     * @return an AuxPoint according to the concrete strategy implementation.
     */
    public AuxPoint getPointFromTreeSet(TreeSet<AuxPoint> set, DateTime eventTimeStamp);
}
