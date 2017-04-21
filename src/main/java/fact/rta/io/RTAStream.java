package fact.rta.io;

import fact.io.zfits.ZFitsStream;
import fact.rta.RTADataBase;
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


    @Parameter(required = true, description = "Path to folder thats being watched")
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
            if (name != null && attr.isRegularFile() && !fileQueue.contains(file)) {
                if (matcher.matches(name)){
                    int night = filenameToFACTNight(file.getFileName().toString()).orElse(0);
                    int runid = filenameToRunID(file.getFileName().toString()).orElse(0);
                    Run run = dbInterface.getRun(night, runid);
                    //analyze run if it doesn't exist or its state is unknown. but avoid duplicates in the queue.
                    if (run == null || run.health == RTADataBase.HEALTH.UNKNOWN){
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
        new Timer().scheduleAtFixedRate(updater, 0, 10* MINUTE);
    }

    private class Updater extends TimerTask{

        @Override
        public void run() {
            Path dir = Paths.get(folder);
            try {
                Files.walkFileTree(dir, new RegexVisitor("\\d{8}_\\d{3}.(fits|fits.fz|fits\\.gz)$"));
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
    }

    private void checkStream() throws Exception {
        String path = fileQueue.take().toFile().getAbsolutePath();
        stream.setUrl(new SourceURL("file:" + path));
        stream.init();
        try{
            ZFitsStream zstream = (ZFitsStream) stream;
            if(zstream.headerItem.containsKey("RUNTYPE")){
                String runtype = zstream.headerItem.get("RUNTYPE").toString();
                if(!runtype.equals("data")){
                    //not a data run. skip
                    log.info("Skipping run with type: " + runtype);
                    checkStream();
                }
            }
        } catch (ClassCastException e){
            //pass
            log.warn("not using a zfits stream as inner stream. this will not skip non-data runs.");
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
        if (stream == null) {
            //get the first stream inside this multistream.
            stream = (AbstractStream) streams.get(additionOrder.get(0));
            checkStream();
        }
        Data data = stream.read();
        if(data == null){
            checkStream();
            data = stream.read();
        }
        return data;
    }
}
