package fact.rta.io;

import fact.io.hdureader.FITSStream;
import fact.rta.RTADataBase;
import fact.rta.WebSocketService;
import fact.rta.db.Run;
import org.skife.jdbi.v2.DBI;
import stream.Data;
import stream.annotations.Parameter;
import stream.io.AbstractStream;
import stream.io.SourceURL;
import stream.io.multi.AbstractMultiStream;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * Created by mackaiver on 21/09/16.
 */
public class RTAStream extends AbstractMultiStream {


    @Parameter(required = true, description = "Path to folder that is being watched")
    public String folder;

    @Parameter
    public String jdbcConnection;

    private AbstractStream stream;

    public BlockingQueue<Path> fileQueue = new LinkedBlockingQueue<>();

    private Updater updater = new Updater();
    private RTADataBase.DBInterface dbInterface;


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


    public class RegexVisitor extends SimpleFileVisitor<Path> {

        private final PathMatcher matcher;

        public RegexVisitor(String pattern) {
            matcher = FileSystems.getDefault().getPathMatcher("regex:" + pattern);
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
            Path name = file.getFileName();
            if (name != null && attr.isRegularFile()) {
                //check whether we already looked at the file.
                if (fileQueue.contains(file)) {
                         return FileVisitResult.CONTINUE;
                }
                if (matcher.matches(name)) {
                    int night = filenameToFACTNight(file.getFileName().toString()).orElse(0);
                    int runid = filenameToRunID(file.getFileName().toString()).orElse(0);
                    Run run = dbInterface.getRun(night, runid);
                    //analyze run if it doesn't exist or its state is unknown. but avoid duplicates in the queue.
                    if (run == null || run.health == RTADataBase.HEALTH.UNKNOWN) {
                        fileQueue.add(file);
                    }
                }

            } else {
                log.info("Not a regular file: {} ", file);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            exc.printStackTrace();
            log.error("Could not visit file: {}. Continuing.", file);
            return FileVisitResult.CONTINUE;
        }
    }


    @Override
    public void init() throws Exception {

        DBI dbi = new DBI(jdbcConnection);
        dbInterface = dbi.open(RTADataBase.DBInterface.class);
        dbInterface.createRunTableIfNotExists();
        dbInterface.createSignalTableIfNotExists();

        long MINUTE = 60 * 1000;
        log.info("Starting file system watcher with interval of 10 Minutes");
        new Timer().scheduleAtFixedRate(updater, 0, 10 * MINUTE);
    }

    private class Updater extends TimerTask{

        @Override
        public void run() {
            Path dir = Paths.get(folder);
            WebSocketService s = WebSocketService.getService();
            if (s == null){
                log.info("Could not get an instance of the websocket service");
            }
            try {
                log.info("Checking file system for new data.");
                if(s != null){
                    s.messageHandler.sendDataStatus("Checking for new data.");
                }
                Files.walkFileTree(dir, new RegexVisitor("\\d{8}_\\d{3}.(fits|fits.fz|fits\\.gz)$"));

                if (fileQueue.isEmpty()){
                    log.info("No new data present.");
                    if (s!=null){
                        s.messageHandler.sendDataStatus("No new data present.");
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException();
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
        }

        String path = fileQueue.take().toFile().getAbsolutePath();

        log.info("Opening file " + path);
        stream.setUrl(new SourceURL("file:" + path));
        stream.init();
        try{
            FITSStream zstream = (FITSStream) stream;
            String runtype = zstream.eventHDUHeader.get("RUNTYPE").orElseThrow(() -> new IOException("No runtype information"));

            if(!runtype.equals("data")){
                //not a data run. skip
                log.info("Skipping run with type: " + runtype);
                checkNextFile();
            }
        } catch (ClassCastException e){
            //pass
            log.warn("Not using the hdureader as input stream. This won't skip non-data runs.");
        }
    }

    //TODO: Maybe just get the latest non analysed file from the db and start working on that. seems much simpler.
    /**
     * This will read all FACT raw files it finds within a folder (recursively). It tries to skip non 'data' runs.
     * This works as long as the inner stream is a ZFitsStream.
     * @return a data item from a raw data file.
     * @throws Exception
     */
    @Override
    public synchronized Data readNext() throws Exception {
        if (this.count % 128 == 0){
            WebSocketService s = WebSocketService.getService();
            if(s != null){
               s.messageHandler.sendDataStatus("Currently streaming data.");
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
