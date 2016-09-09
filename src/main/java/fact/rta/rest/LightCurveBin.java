package fact.rta.rest;

import org.joda.time.DateTime;

/**
 * Created by mackaiver on 07/09/16.
 */
public class LightCurveBin {
    public final DateTime startTime;
    public final DateTime endTime;
    public final int backgroundEvents;
    public final int signalEvents;
    public final double alpha;
    public final double onTimeInBin;


    public LightCurveBin(DateTime startTime, DateTime endTime, int backgroundEvents, int signalEvents, double alpha, double onTimeInBin){
        this.startTime = startTime;
        this.endTime = endTime;
        this.backgroundEvents = backgroundEvents;
        this.signalEvents = signalEvents;
        this.alpha = alpha;
        this.onTimeInBin = onTimeInBin;
    }
}
