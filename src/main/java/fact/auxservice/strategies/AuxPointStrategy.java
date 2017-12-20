package fact.auxservice.strategies;

import fact.auxservice.AuxPoint;

import java.time.ZonedDateTime;
import java.util.TreeSet;

/**
 * An AuxPointStrategy defines which point from a given TreeSet should be returned.
 * You could also interpolate between two results and return a new interpolated Point.
 * This can return null if the data doesn't exist in this context. For example if the File from which the data
 * was read does not contain a valid point for that eventTimeStamp.
 * <p>
 * This interface also defines convenience functions for creating these strategy objects.
 * <p>
 * Created by kai on 01.04.15.
 */
public interface AuxPointStrategy {
    /**
     * @param set            the set from which to get the points
     * @param eventTimeStamp the timestamp for which you want the auxiliary data
     * @return an AuxPoint according to the concrete strategy implementation.
     */
    AuxPoint getPointFromTreeSet(TreeSet<AuxPoint> set, ZonedDateTime eventTimeStamp);

    /**
     * Convenience method to create Strategy object
     *
     * @return strategy to get the closest AuxPoint in Time.
     */
    static AuxPointStrategy Closest() {
        return new Closest();
    }


    /**
     * Convenience method to create Strategy object
     *
     * @return strategy to get the closest AuxPoint that comes earlier in Time.
     */
    static AuxPointStrategy Earlier() {
        return new Earlier();
    }


    /**
     * Convenience method to create Strategy object
     *
     * @return strategy to get the closest AuxPoint that comes later in Time.
     */
    static AuxPointStrategy Later() {
        return new Later();
    }
}
