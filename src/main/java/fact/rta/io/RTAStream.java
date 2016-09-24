package fact.rta.io;

import fact.rta.RTADataBase;
import fact.rta.RTAWebService;
import fact.rta.db.Run;
import stream.Data;
import stream.annotations.Parameter;
import stream.annotations.Service;
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
    private String pathName;


    private AbstractStream stream;

    public BlockingQueue<Path> fileQueue = new LinkedBlockingQueue<>();

    @Service
    public RTAWebService webService;

    private Updater updater = new Updater();


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
                    Run run = webService.dbInterface.getRun(night, runid);
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
        super.init();

        long MINUTE = 60 * 1000;
        new Timer().scheduleAtFixedRate(updater, 0, 10* MINUTE);
    }

    private class Updater extends TimerTask{

        @Override
        public void run() {
            Path dir = Paths.get(pathName);
            try {
                Files.walkFileTree(dir, new RegexVisitor("\\d{8}_\\d{3}.(fits|zfits|fits\\.gz)$"));
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
    }

    //TODO: Maybe just get the latest non analysed file from hte db and start working on that. seems much simpler.
    @Override
    public synchronized Data readNext() throws Exception {
        if (stream == null) {
            //get the first stream inside this multistream.
            stream = (AbstractStream) streams.get(additionOrder.get(0));
            String path = fileQueue.take().toFile().getAbsolutePath();
            stream.setUrl(new SourceURL(path));
            stream.init();
        }

        Data data = stream.readNext();
        if(data == null){
            String path = fileQueue.take().toFile().getAbsolutePath();
            stream.setUrl(new SourceURL(path));
            stream.init();
            return stream.readNext();
        }
        return data;
    }
}
