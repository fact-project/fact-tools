package fact.rta;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeMap;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;

/**
 * Use number of signal and background events to calculate a light curve.
 *
 * Created by kai on 24.01.16.
 */
public class LightCurve implements Processor {
    //TODO: use service annotation to inject rtawebservice
    static Logger log = LoggerFactory.getLogger(LightCurve.class);

    @Parameter(required = true)
    RTAWebService webService;


    @Parameter(required = false , description = "Binning in minutes")
    int binning = 1;

    @Parameter(required = false , description = "Keep bins that are *history* minutes old and delete older ones")
    int history = 20;

    @Parameter(required = false , description = "Number of off regions that are used to measure background")
    int offRegions = 5;

    private TreeRangeMap<DateTime, SignalContainer> lightCurve = TreeRangeMap.create();

    Range<DateTime> edgesOfCurrentBin;

    class SignalContainer{
        public Integer signalEvents = 0;
        public Integer backgroundEvents = 0;


        public final DateTime timestamp;
        public final Duration duration;
        public final int numberOfOffRegions;

        SignalContainer(Integer signalEvents, Integer backgroundEvents, DateTime timestamp, Duration duration, int numberOfOffRegions) {
            this.signalEvents = signalEvents;
            this.backgroundEvents = backgroundEvents;
            this.timestamp = timestamp;

            this.duration = duration;
            this.numberOfOffRegions = numberOfOffRegions;
        }


        public Timestamp getTimestampAsSQLTimeStamp() {
            return new Timestamp(timestamp.getMillis());
        }

        public int getDurationInSeconds() {
            return duration.toStandardSeconds().getSeconds();
        }


    }

    /**
     * Takes the int[2] array found in the FITs files under the name UnixTimeUTC and converts it to a DateTime
     * instance with time zone UTC. If the passed array cannot be converted the optional will be empty.
     *
     * @param eventTime the UnixTimeUTC array as found in the FITS file.
     * @return an Optional containing the Datetime instance
     */
    public  static Optional<DateTime> unixTimeUTCToDateTime(int [] eventTime){
        if(eventTime != null && eventTime.length == 2) {
            DateTime timeStamp = new DateTime((long)((eventTime[0]+eventTime[1]/1000000.)*1000), DateTimeZone.UTC);
            return Optional.of(timeStamp);
        }
        return Optional.empty();
    }


    @Override
    public Data process(Data data) {

        int[] unixTimeUTC = (int[]) data.get("UnixTimeUTC");

        DateTime eventTimeStamp = unixTimeUTCToDateTime(unixTimeUTC).
                                        orElseThrow(() -> new IllegalArgumentException("No valid timestamp in event."));


        if (!data.containsKey("@signal")){
            log.warn("No signal in event. Ignoring.");
            return data;
        }
        int signal = (int) data.get("@signal");
        int background = (int) data.get("@background");

        //check if we have a new bin. If we start a new bin the excess rate is still zero
        //TODO: think whether we should use the event timestamp here. maybe also watch the delay?
        DateTime time = DateTime.now();
        SignalContainer container = lightCurve.get(time);
        if (container != null){
            container.signalEvents += signal;
            container.backgroundEvents += background;
            lightCurve.put(edgesOfCurrentBin, container);
        } else {
            edgesOfCurrentBin = Range.closedOpen(time, time.plusSeconds(binning));
            SignalContainer c = new SignalContainer(signal, background, eventTimeStamp, Duration.standardMinutes(binning), offRegions);
            lightCurve.put(edgesOfCurrentBin, c);

            webService.updateLightCurve(lightCurve);
        }



        // remove old bins so we don't accumulate too many old results in memory
        Map.Entry<Range<DateTime>, SignalContainer> entry = lightCurve.getEntry(time.minusMinutes(binning + history));
        if (entry != null){
            lightCurve.remove(entry.getKey());
        }



        if (data.containsKey("@datarate")){
            webService.updateDatarate((double) data.get("@datarate"));
        }


        data.put("@timestamp", eventTimeStamp.toString());
        return data;
    }


    public void setOffRegions(int offRegions) {
        this.offRegions = offRegions;
    }

    public void setBinning(int binning) {
        this.binning = binning;
    }

    public void setHistory(int history) {
        this.history = history;
    }

    public void setWebService(RTAWebService webService) {
        this.webService = webService;
    }
}
