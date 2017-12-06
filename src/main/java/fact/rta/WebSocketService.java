package fact.rta;

import fact.auxservice.AuxFileService;
import fact.auxservice.AuxPoint;
import fact.auxservice.AuxiliaryService;
import fact.auxservice.AuxiliaryServiceName;
import fact.rta.db.Run;
import fact.rta.db.Signal;
import fact.rta.rest.Event;
import fact.rta.rest.StatusContainer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;
import stream.Data;
import stream.io.SourceURL;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

/**
 *
 */
public class WebSocketService{

    final private static Logger log = LoggerFactory.getLogger(WebSocketService.class);

    public Jdbi dbInterface;
    private Run currentRun = null;
    private Deque<Signal> signals = new ArrayDeque<>();
    public MessageHandler messageHandler = new MessageHandler();
    AuxFileService auxFileService;


    //behold the most ugly workaround in history.
    private static WebSocketService service;
    public static WebSocketService getService(String jdbcConnection, SourceURL auxFolder){
        if (service == null){
            log.info("Returning new singleton instance for WebSocketService");
            service = new WebSocketService(jdbcConnection, auxFolder);
        }
        return service;
    }
    public static WebSocketService getService(){
        return service;
    }

    private WebSocketService(String jdbcConnection, SourceURL auxFolder){
        log.info("Constructing a WebSocketService");
        Spark.webSocket("/rta", WebSocket.class);

        Spark.staticFiles.location("/rta/static/");
        Spark.init();

        //update systemstatus once per minute
        long MINUTE = 1000*60;

        final Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                messageHandler.sendStatus(StatusContainer.create());
            }
        }, (long) (0.05 * MINUTE), (long) (0.1 * MINUTE));

        dbInterface = Jdbi.create(jdbcConnection);
        dbInterface.installPlugin(new SqlObjectPlugin());

        dbInterface.useExtension(RTADataBase.class, dao -> {
            dao.createRunTableIfNotExists();
            dao.createSignalTableIfNotExists();
        });

        auxFileService = new AuxFileService();
        auxFileService.auxFolder = auxFolder;
    }


    public void updateDataRate(OffsetDateTime timeStamp, Double dataRate){
        messageHandler.sendDataRate(timeStamp, dataRate);
    }

    /**
     * This method should be called for each 'gamma-like' event in the stream. It will update the appropriate
     * information in the database and calculate 'ontime' for new runs.
     *
     * @param eventTimeStamp a timestamp by which to identify this event
     * @param item the data item for the event
     */
    public void updateEvent(ZonedDateTime eventTimeStamp, Data item) throws IOException {
        ZonedDateTime dT = AuxiliaryService
                .unixTimeUTCToDateTime(item)
                .orElseThrow(() -> new RuntimeException("Could not get time stamp from current event."));

        SortedSet<AuxPoint> ftmPointsForNight = auxFileService.getAuxiliaryDataForWholeNight(AuxiliaryServiceName.FTM_CONTROL_TRIGGER_RATES, dT);

        Run newRun = new Run(item);
        if (currentRun == null){

            dbInterface.useExtension(RTADataBase.class, dao -> {
                dao.insertRun(newRun);
            });

            log.info("Sending runinfo to clients");
            messageHandler.sendRunInfo(newRun);
            currentRun = newRun;
        }
        else if (!currentRun.equals(newRun)) {

            //found a new run. calculate the ontime for the old one and update it in the data base
            Duration onTime = calculateOnTimeForRun(currentRun, ftmPointsForNight);

            dbInterface.useExtension(RTADataBase.class, dao -> {
                dao.updateRunWithOnTime(currentRun, onTime.getSeconds());

                log.info("New run found. OnTime of old run was: {} seconds.", onTime.getSeconds());

                //Save signals to database by looping over the collection and popping the elements
                //to remove them at the same time.
                dao.insertSignals(signals.iterator());
                signals.clear();

                dao.updateRunHealth(RTADataBase.HEALTH.OK, currentRun);

                //insert new run to db
                dao.insertRun(newRun);
                dao.updateRunHealth(RTADataBase.HEALTH.IN_PROGRESS, newRun);
            });


            //send information about the new run around.
            log.info("Sending runinfo to clients");
            messageHandler.sendRunInfo(newRun);

            currentRun = newRun;
        }
        Signal signal = new Signal(eventTimeStamp, ZonedDateTime.now(ZoneOffset.UTC), item, currentRun);
        signals.add(signal);
        if (signal.prediction > 0.7) {
            messageHandler.sendEvent(new Event(eventTimeStamp, item));
        }
    }

    /**
     * Calculate the 'ontime' for a given run from the provided set of AuxPoints from the
     * FTM aux file.
     * Note that still is an approximation. Its a glorified version of summing
     * up all "OnTime" points in the aux file between "RunStart" and "RunEnd"
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
                    ZonedDateTime t = p.getTimeStamp();

                    boolean afterStart = t.isAfter(run.start_time);
                    boolean beforeEnd = t.isBefore(run.end_time);
                    return afterStart && beforeEnd;
                })
                .mapToDouble(p -> p.getFloat("OnTime"))
                .sum() * 1000;

        return Duration.ofMillis((long) (onTimeInMilliSeconds));
    }

}
