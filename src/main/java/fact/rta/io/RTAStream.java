package fact.rta.io;

import fact.io.hdureader.FITSStream;
import fact.rta.RTADataBase;
import fact.rta.WebSocketService;
import fact.rta.db.Run;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.annotations.Parameter;
import stream.io.AbstractStream;
import stream.io.SourceURL;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

/**
 *
 * Created by mackaiver on 21/09/16.
 */
public class RTAStream extends AbstractStream {

    final private static Logger log = LoggerFactory.getLogger(RTAStream.class);

    @Parameter(required = true, description = "Path to folder that is being watched")
    public String folder;

    @Parameter(required = true, description = "jdcb connection string. e.g. jdbc:sqlite:rta.sqlite")
    public String jdbcConnection;

    @Parameter(required = true, description = "The url pointing to the path containing a the auxilary " +
            "data in FACTS canonical folder structure." )
    public SourceURL auxFolder;

    private FITSStream fitsStream = new FITSStream();

    BlockingQueue<Path> fileQueue = new LinkedBlockingQueue<>();


    static Optional<Integer> filenameToRunID(String filename){
        String[] split = filename.split("_|\\.");
        try {
            return Optional.of(Integer.parseInt(split[1]));
        } catch (NumberFormatException e){
            return Optional.empty();
        }
    }

    static Optional<Integer> filenameToFACTNight(String filename){
        String[] split = filename.split("_|\\.");
        try {
            return Optional.of(Integer.parseInt(split[0]));
        } catch (NumberFormatException e){
            return Optional.empty();
        }
    }


    @Override
    public void init() throws Exception {
        long SECOND = 1000L;
        long MINUTE = 60 * 1000;
        log.info("Starting file system watcher with interval of 10 Minutes");
        WebSocketService s = WebSocketService.getService(jdbcConnection, auxFolder);
        new Timer().scheduleAtFixedRate(new Updater(), 0, 10 * MINUTE);
        new Timer().scheduleAtFixedRate(new Reporter(), 10 * SECOND, 2 * MINUTE);
    }

    private class Reporter extends TimerTask{
        @Override
        public void run() {
            WebSocketService s = WebSocketService.getService();
            if (s == null){
                log.info("Could not get an instance of the websocket service");
                return;
            }
            if(fileQueue.isEmpty()){
                s.messageHandler.sendDataStatus("Waiting for new Data.");
                return;
            }
            s.messageHandler.sendDataStatus(String.format("Currently %d files in queue.", fileQueue.size()));
        }
    }


    private class Updater extends TimerTask{

        @Override
        public void run() {
            Path dir = Paths.get(folder);

            WebSocketService s = WebSocketService.getService();
            if (s == null){
                log.info("Could not get an instance of the websocket service");
            } else {

                if (s.dbInterface == null){
                    return;
                }

                try {
                    log.info("Checking file system for new data.");
                    s.messageHandler.sendDataStatus("Checking for new data.");

                    PathMatcher regex = FileSystems
                            .getDefault()
                            .getPathMatcher("regex:\\d{8}_\\d{3}.(fits|zfits)(.gz|.fz)?");


                    List<Path> paths = Files.walk(dir)
                            .filter(Files::isRegularFile)
                            .filter(Files::isReadable)
                            .filter(p ->regex.matches(p.getFileName()))
                            .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                            .collect(toList());

                    s.dbInterface.useExtension(RTADataBase.class, dao -> {
                        for (Path p : paths) {
                            int night = filenameToFACTNight(p.getFileName().toString()).orElse(0);
                            int runid = filenameToRunID(p.getFileName().toString()).orElse(0);
                            Run run = dao.getRun(night, runid);
                            //analyze run if it doesn't exist or its state is unknown. but avoid duplicates in the queue.
                            if (run == null || run.health == RTADataBase.HEALTH.UNKNOWN) {
                                if (! fileQueue.contains(p)) {
                                    fileQueue.add(p);
                                }
                            } else{
                                log.info("File {} already marked as analyzed in DB", p);
                            }
                        }
                    });

                    if (fileQueue.isEmpty()) {
                        log.info("No new data present.");
                        s.messageHandler.sendDataStatus("No new data present.");
                    }

                } catch (IOException e) {
                    throw new RuntimeException();
                }
            }
        }
    }

    private void takeNextFile() throws Exception {
        String runtype = "";
        while (!runtype.equals("data")) {
            Path nextFile = fileQueue.take();
            fitsStream.setUrl(new SourceURL("file:" + nextFile.toString()));

            RetryPolicy retryPolicy = new RetryPolicy()
                    .retryOn(FileNotFoundException.class)
                    .retryOn(NullPointerException.class)
                    .withDelay(2, TimeUnit.SECONDS)
                    .withMaxRetries(15);

            Failsafe
                    .with(retryPolicy)
                    .onFailedAttempt(failure ->{
                        log.warn("Could not open file at {} trying next file in 15 seconds", nextFile);
                        fitsStream.setUrl(new SourceURL("file:" + fileQueue.take().toString()));
                    })
                    .run(() -> fitsStream.init());

            runtype= fitsStream.eventHDU
                                .header
                                .get("RUNTYPE")
                                .orElse("broken");

            if (!runtype.equals("data")) {
                log.info("Skipping run with type: " + runtype);
            }
            if (runtype.equals("broken")) {
                log.info("Could not get runtype from fitsfile: " + fitsStream.getUrl().getFile());
            }
        }
        log.info("Opening File {}", fitsStream.getUrl().getFile());
    }


    private RetryPolicy pollingPolicy = new RetryPolicy()
            .retryIf(result -> result == null)
            .withDelay(5, TimeUnit.SECONDS)
            .withJitter(500, TimeUnit.MILLISECONDS);

    /**
     * This will read all FACT raw files it finds within a folder (recursively).
     * @return a data item from a raw data file.
     * @throws Exception hopefully not very often.
     */
    @Override
    public synchronized Data readNext() throws Exception {
        if (fitsStream.getUrl() ==  null){
            takeNextFile();
        }

        return Failsafe.with(pollingPolicy)
                .onFailedAttempt((a, failure) -> {
                    takeNextFile();
                })
                .get(() -> fitsStream.readNext());
    }
}
