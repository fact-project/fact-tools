package fact.auxservice.drivepoints;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;

/**
 * A TrackingPoint contains all the information from the telescopes drive at on specific point in time.
 * We introduce an artificial order on TrackingPoints by comparing the 'time' attribute of two tracking points to
 * each other using the canonical definition of 'earlier' and 'later'
 * All angles in a tracking point are given in degrees.
 * The time is given as JulianDay
 *
 * Created by kaibrugge on 06.10.14.
 *
 */
public abstract class DrivePoint implements Comparable<Double>{
    Logger log = LoggerFactory.getLogger(DrivePoint.class);

    //Julian Day
    double time;

    public abstract void initialiseWithDataItem(Data item) throws IllegalArgumentException;

    @Override
    public int compareTo(Double o) {
        return Double.compare(time, o);
    }

    public double distanceTo(double t){
        return Math.abs(time - t);
    }
}