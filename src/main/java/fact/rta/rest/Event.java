package fact.rta.rest;

import java.io.Serializable;
import java.time.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mackaiver on 20/12/16.
 */
public class Event {

    final double[] photonCharges;
    final double estimatedEnergy;
    final double size;
    final double thetaSquare;
    final String sourceName;
    final ZonedDateTime eventTimeStamp;
    final String dateString;

    public Event(ZonedDateTime eventTimeStamp, Map<String, Serializable> item) {
        this.thetaSquare = (double) item.get("signal:thetasquare");
        this.estimatedEnergy = (double) item.get("energy");
        this.size = (double) item.get("Size");
        this.sourceName = (String) item.get("SourceName");
        this.photonCharges = (double[]) item.get("photoncharge");
        this.eventTimeStamp = eventTimeStamp;
        this.dateString = eventTimeStamp.toString();
    }

    public static Event createEmptyEvent(){

        ZonedDateTime eventTimeStamp = ZonedDateTime.now(ZoneOffset.UTC);
        double[] photons = new double[1440];
        for (int i = 0; i < photons.length; i++) {
            photons[i] = i;
        }

        Map<String, Serializable> m = new HashMap<>();
        m.put("photoncharge", photons);
        m.put("Size", 0.0);
        m.put("energy", 0.0);
        m.put("SourceName", "No Source");
        m.put("signal:thetasquare",  0.0);

        return new Event(eventTimeStamp, m);

    }

}
