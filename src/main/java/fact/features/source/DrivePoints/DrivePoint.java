package fact.features.source.drivepoints;

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
public class DrivePoint implements Comparable<Double>{
    Logger log = LoggerFactory.getLogger(DrivePoint.class);

    //Julian Day
    double time;

    public DrivePoint(Data item) throws IllegalArgumentException{
        time =	Double.parseDouble(item.get("Time").toString()) + 2440587.5;
        if (time <= 2451544.5){
            throw new IllegalArgumentException("Dates before 1.1.2000 are not supported");
        }
    }

    public DrivePoint(double time) throws IllegalArgumentException{
        if (time <= 2451544.5){
            throw new IllegalArgumentException("Dates before 1.1.2000 are not supported");
        }
        this.time = time;
    }

    @Override
    public int compareTo(Double o) {
        return Double.compare(time, o);
    }

    public double distanceTo(double t){
        return Math.abs(time - t);
    }
}