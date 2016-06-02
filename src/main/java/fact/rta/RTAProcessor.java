package fact.rta;

import com.google.common.collect.Range;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Keys;
import stream.Processor;
import stream.annotations.Parameter;
import stream.annotations.Service;

import java.io.IOException;
import java.util.Optional;

/**
 * Use number of signal and background events to calculate a light curve.
 *
 * Created by kai on 24.01.16.
 */
public class RTAProcessor implements Processor {
    //TODO: use service annotation to inject rtawebservice
    static Logger log = LoggerFactory.getLogger(RTAProcessor.class);

    @Service(required = true)
    RTAWebService webService;

    @Parameter(required = false , description = "Binning in minutes")
    int binning = 1;

    @Parameter(required = false , description = "Keep bins that are *history* minutes old and delete older ones")
    int history = 180;

    @Parameter(required = false , description = "Number of off regions that are used to measure background")
    int offRegions = 5;

    double thetaCut = 0.1;
    double predictionThreshold = 0.7;
    private Range<DateTime> edgesOfCurrentBin;
    private SignalContainer container = new SignalContainer(0, 0, offRegions, predictionThreshold, thetaCut);

    public class SignalContainer{
        public Integer signalEvents = 0;
        public Integer backgroundEvents = 0;

        public final int numberOfOffRegions;
        public final double predictionThreshold;
        public final double thetaCut;

        public SignalContainer(Integer signalEvents, Integer backgroundEvents, int numberOfOffRegions, double predictionThreshold, double thetaCut) {
            this.signalEvents = signalEvents;
            this.backgroundEvents = backgroundEvents;
            this.numberOfOffRegions = numberOfOffRegions;
            this.predictionThreshold = predictionThreshold;
            this.thetaCut = thetaCut;
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

    private int background(Data data, double thetaCut){
        int backGroundEvents = 0;

        Keys offKeys = new Keys("Theta_Off_?");
        for (String key : offKeys.select(data)) {
            double offValue = (double) data.get(key);
            if (offValue > thetaCut){
                backGroundEvents += 1;
            }
        }
        return backGroundEvents;
    }

    private int signal(Data data, double predictionThreshold, double thetaCut){
        double prediction = (double) data.get("signal:prediction");
        double theta = (double) data.get("signal:thetasquare");
        if (prediction <= predictionThreshold && theta <= thetaCut){
            return 1;
        }
        return 0;
    }

    @Override
    public Data process(Data data) {

        int[] unixTimeUTC = (int[]) data.get("UnixTimeUTC");

        DateTime eventTimeStamp = unixTimeUTCToDateTime(unixTimeUTC).
                                        orElseThrow(() -> new IllegalArgumentException("No valid eventTimeStamp in event."));

        data.put("@eventTimeStamp", eventTimeStamp.toString());

        String sourceName = (String) data.get("SourceName");

        if (!data.containsKey("signal:prediction")){
//            log.warn("No signal in event. Ignoring.");
            return data;
        }


        int signal = signal(data, predictionThreshold, thetaCut);
        if (signal == 0){
            return data;
        }
        updateWebService(data, eventTimeStamp);

        int background = background(data, thetaCut);

        //check if we have need a new bin.
        if (edgesOfCurrentBin != null && edgesOfCurrentBin.contains(eventTimeStamp)){
            container.signalEvents += signal;
            container.backgroundEvents += background;
        } else {
            Range<DateTime> dateTimeRange = Range.closedOpen(eventTimeStamp, eventTimeStamp.plusMinutes(binning));

            container = new SignalContainer(signal, background, offRegions, predictionThreshold, thetaCut);
            edgesOfCurrentBin = dateTimeRange;

            try {
                log.info("Updating lc");
                webService.updateLightCurve(dateTimeRange, container, sourceName);
            } catch (IOException e) {
                log.error("Error while updating webservice");
                throw new RuntimeException("Error while updating webservice");
            }

        }

        return data;
    }

    private void updateWebService(Data data, DateTime eventTimeStamp) {
        System.out.println("updating event");
        double thetaSquare = (double) data.get("signal:thetasquare");
        double[] photoncharges = (double[]) data.get("photoncharge");
        double estimatedEnergy = (double) data.get("energy");
        double size = (double) data.get("Size");
        String sourceName = (String) data.get("SourceName");
        webService.updateEvent(photoncharges, estimatedEnergy, size, thetaSquare, sourceName, eventTimeStamp);
    }
}
