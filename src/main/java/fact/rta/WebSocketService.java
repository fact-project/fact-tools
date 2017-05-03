package fact.rta;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import fact.auxservice.AuxFileService;
import fact.auxservice.AuxPoint;
import fact.auxservice.AuxiliaryService;
import fact.auxservice.AuxiliaryServiceName;
import fact.auxservice.strategies.AuxPointStrategy;
import fact.rta.db.*;
import fact.rta.db.Signal;
import org.eclipse.jetty.websocket.api.Session;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;
import stream.Data;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 *
 */
public class WebSocketService extends RTAWebService implements AuxiliaryService {

    final private static Logger log = LoggerFactory.getLogger(WebSocketService.class);

    static Set<Session> sessions = Sets.newConcurrentHashSet();
    RTADataBase.DBInterface dbInterface;
    private Run currentRun = null;
    Deque<Signal> signals = new ArrayDeque<>();
    private boolean isInit = false;
    private AuxFileService auxService;



    @Parameter(required = true, description = "Path to the .sqlite file")
    String jdbcConnection;

    @Parameter(required = true, description = "The url pointing to the path containing a the auxilary " +
            "data in FACTS canonical folder structure." )
    public SourceURL auxFolder;

    public WebSocketService(String args){}

    public WebSocketService(){
        Spark.webSocket("/datarate", DataRateWebSocketHandler.class);
        Spark.staticFiles.location("/rta/");
        Spark.init();
    }

    public void updateDataRate(OffsetDateTime timeStamp, Double dataRate){

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("timestamp", timeStamp.toString());
        jsonObject.addProperty("rate", dataRate);
        sessions.stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(jsonObject.toString());
            } catch (IOException e) {
                log.error("Error sending datarate to session " + session);
            }
        });
    }

    /**
     * I need an init method here since streams does not have a proper lifecycle for service objects.
     * I cannot initialize the DBI interface in the constructor since I don't know the jdbc conncection
     * at that point. It will be set after the constructor has been called.
     *
     */
    public void init(){
        dbInterface = new DBI(this.jdbcConnection).open(RTADataBase.DBInterface.class);

        dbInterface.createRunTableIfNotExists();
        dbInterface.createSignalTableIfNotExists();


        auxService = new AuxFileService();
        auxService.auxFolder = auxFolder;
        isInit = true;
    }


    /**
     * This method should be called for each 'gamma-like' event in the stream. It will update the appropriate
     * information in the database and calculate 'ontime' for new runs.
     *
     *
     * @param eventTimeStamp a timestamp by which to identify this event
     * @param item the data item for the event
     */
    @Override
    public void updateEvent(OffsetDateTime eventTimeStamp, Data item) throws IOException {

        DateTime dT = AuxiliaryService
                .unixTimeUTCToDateTime(item)
                .orElseThrow(() -> new RuntimeException("Could not get time stamp from current event."));

        SortedSet<AuxPoint> ftmPointsForNight = auxService.getAuxiliaryDataForWholeNight(AuxiliaryServiceName.FTM_CONTROL_TRIGGER_RATES, dT);



        Run newRun = new Run(item);
        if (currentRun == null){
            dbInterface.insertRun(newRun);
            currentRun = newRun;
        }
        else if (!currentRun.equals(newRun)) {

            //found a new run. calculate the ontime for the old one and update it in the data base
            Duration onTime = calculateOnTimeForRun(currentRun, ftmPointsForNight);

            dbInterface.updateRunWithOnTime(currentRun, onTime.getSeconds());
            log.info("New run found. OnTime of old run was: {} seconds.", onTime);

            //Save signals to database by looping over the collection and poping the elements
            //to remove them at the same time.
            //TODO: Can this be a bulk operation?
            while(signals.size() > 0){
                dbInterface.insertSignal(signals.pop());
            }

            dbInterface.updateRunHealth(RTADataBase.HEALTH.OK, currentRun.runID, currentRun.night);

            //insert new run to db
            dbInterface.insertRun(newRun);
            dbInterface.updateRunHealth(RTADataBase.HEALTH.IN_PROGRESS, newRun.runID, newRun.night);

            currentRun = newRun;

        }

        signals.add(new Signal(eventTimeStamp, OffsetDateTime.now(ZoneOffset.UTC), item, currentRun));
    }

    /**
     * Calculate the 'ontime' for a given run from the provided set of AuxPoints from the
     * FTM aux file.
     * Note that still is an approximation. Its a glorified version of summmin
     * up all "OnTime" points in the aux file between "RunStart" and "RunEnd"
     *
     *
     * @param run The run to calculate the 'ontime' for
     * @param ftmPoints Collection of AuxPoints containing the FTM measurements taken during the run
     * @return the Duration during which the trigger was alive.
     */
    private Duration calculateOnTimeForRun(Run run, Set<AuxPoint> ftmPoints){
        //I underestimate the actual ontime this way. One could interpolate between the points between two runs.
        double onTimeInMilliSeconds = ftmPoints
                .stream()
                .filter(p -> {
                    OffsetDateTime t = p.getTimeStamp().toGregorianCalendar().toZonedDateTime().toOffsetDateTime();

                    boolean after = t.isAfter(run.startTime);
                    boolean before = t.isAfter(run.endTime);
                    return after && before;
                })
                .mapToDouble(p -> p.getFloat("OnTime"))
                .sum() * 1000;

        return Duration.ofMillis((long) (onTimeInMilliSeconds));
    }


    @Override
    public void reset() throws Exception {

    }

    /**
     * Delegating call to the method in the AuxFileService.
     *
     * @see AuxiliaryService#getAuxiliaryData(AuxiliaryServiceName, DateTime, AuxPointStrategy)
     *
     * @param serviceName the name of the aux data to access. This is written in the filename 20130112.<serviceName>.fits
     * @param eventTimeStamp the DateTime of the event you need the aux data for.
     * @param strategy one of the strategies implemented for fetching aux points
     * @return the AuxPoint.
     * @throws IOException propagates the exception of the underlying AuxFileService
     */
    @Override
    public AuxPoint getAuxiliaryData(AuxiliaryServiceName serviceName, DateTime eventTimeStamp, AuxPointStrategy strategy) throws IOException {
        if (!isInit){
            init();
        }
        return auxService.getAuxiliaryData(serviceName, eventTimeStamp, strategy);
    }
}
