package fact.rta.io;

import fact.io.hdureader.FITSStream;
import fact.rta.RTADataBase;
import fact.rta.WebSocketService;
import fact.rta.db.Run;
import org.jdbi.v3.core.Jdbi;
import stream.Data;
import stream.annotations.Parameter;
import stream.io.AbstractStream;
import stream.io.SourceURL;
import stream.io.multi.AbstractMultiStream;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.stream.Collectors.toList;

/**
 *
 * Created by mackaiver on 21/09/16.
 */
public class RTAStream extends AbstractMultiStream {


    @Parameter(required = true, description = "Path to folder that is being watched")
    public String folder;

    private AbstractStream stream;

    public BlockingQueue<Path> fileQueue = new LinkedBlockingQueue<>();

    Updater updater = new Updater();


    public static Optional<Integer> filenameToRunID(String filename){
        String[] split = filename.split("_|\\.");
        try {
            return Optional.of(Integer.parseInt(split[1]));
        } catch (NumberFormatException e){
            return Optional.empty();
        }
    }

    public static Optional<Integer> filenameToFACTNight(String filename){
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
        new Timer().scheduleAtFixedRate(updater, 30 * SECOND, 10 * MINUTE);
    }

    class Updater extends TimerTask{

        @Override
        public void run() {
            Path dir = Paths.get(folder);
            WebSocketService s = WebSocketService.getService();
            if (s == null){
                log.info("Could not get an instance of the websocket service");
            } else {
                try {
                    log.info("Checking file system for new data.");
                    s.messageHandler.sendDataStatus("Checking for new data.");

                    PathMatcher regex = FileSystems
                            .getDefault()
                            .getPathMatcher("regex:\\d{8}_\\d{3}.(fits|zfits)(.gz)?");


                    List<Path> paths = Files.walk(dir)
                            .filter(path -> Files.isRegularFile(path))
                            .filter(Files::isReadable)
                            .map(Path::getFileName)
                            .filter(regex::matches)
                            .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                            .collect(toList());

                    s.dbInterface.useExtension(RTADataBase.class, dao -> {
                        for (Path p : paths) {
                            int night = filenameToFACTNight(p.getFileName().toString()).orElse(0);
                            int runid = filenameToRunID(p.getFileName().toString()).orElse(0);
                            Run run = dao.getRun(night, runid);
                            //analyze run if it doesn't exist or its state is unknown. but avoid duplicates in the queue.
                            if (run == null || run.health == RTADataBase.HEALTH.UNKNOWN) {
                                fileQueue.add(p);
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

    private void checkNextFile() throws Exception {
        WebSocketService s = WebSocketService.getService();
        if(fileQueue.isEmpty()){
            log.info("FileQueue is empty. Waiting for new data. ");
            if (s != null){
                    s.messageHandler.sendDataStatus("Waiting for new data.");
            }
            else{
                log.info("No WebService available");
            }
        }

        //this operation waits for new data to become available
        String path = fileQueue.take().toFile().getAbsolutePath();

        log.info("Opening file " + path);
        stream.setUrl(new SourceURL("file:" + path));
        try{
            stream.init();
            FITSStream zstream = (FITSStream) stream;

            String runtype = zstream.eventHDU.header.get("RUNTYPE").orElseThrow(() -> new IOException("No runtype information"));

            if(!runtype.equals("data")){
                //not a data run. skip
                log.info("Skipping run with type: " + runtype);
                checkNextFile();
            }
        } catch (ClassCastException e){
            //pass
            log.warn("Not using the hdureader as input stream. This won't skip non-data runs.");
        } catch (Exception e){
            log.warn("File failed to read. Skipping");
            e.printStackTrace();
            checkNextFile();
        }
    }

    //TODO: Maybe just get the latest non analysed file from the db and start working on that. seems much simpler.
    /**
     * This will read all FACT raw files it finds within a folder (recursively).
     * @return a data item from a raw data file.
     * @throws Exception
     */
    @Override
    public synchronized Data readNext() throws Exception {
        if (this.count % 128 == 0){
            WebSocketService s = WebSocketService.getService();
            if(s != null){
                s.messageHandler.sendDataStatus("Currently streaming data.");
            } else {
                log.info("no WebService available on readnext");
            }
        }

        if (stream == null) {
            //get the first stream inside this multistream.
            stream = (AbstractStream) streams.get(additionOrder.get(0));
            checkNextFile();
        }
        Data data = stream.read();
        if(data == null){
            checkNextFile();
            data = stream.read();
        }
        return data;
    }
}
