package fact.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.annotations.Parameter;
import stream.io.AbstractStream;
import stream.io.SourceURL;
import stream.io.multi.AbstractMultiStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Given a glob like a pattern describing files a folder _relative_ to the provided url, starts a stream working on
 * all files sequentially.
 * <p>
 * For example:
 * <p>
 * <stream id="fact" class="fact.io.RecursiveDirectoryStream"   url="file:/some/folder" pattern="**&#47;*_Events.fits.gz*" >
 * <stream class="fact.io.FITSStream" id="_"/>
 * </stream>
 * <p>
 * finds all files below (recursively) /some/folder with their names matching the pattern. See unix globs for more
 * information on pattern syntax
 * <p>
 * Created by mackaiver on 12/15/15.
 */
public class RecursiveDirectoryStream extends AbstractMultiStream {
    static Logger log = LoggerFactory.getLogger(RecursiveDirectoryStream.class);


    public BlockingQueue<Path> files = new LinkedBlockingQueue<>();

    @Parameter(required = false, description = "Maximum depth of folders to traverse", defaultValue = "6")
    public int maxDepth = 6;

    @Parameter(required = true, description = "The pattern to filter files by. Understands usual glob syntax")
    public String pattern;

    //counts how many files have been processed
    private int filesCounter = 0;
    private int failedFilesCounter = 0;
    private List<String> failedFilesList = new ArrayList<>();
    @Parameter(required = false, description = "If false the reading of a broken file throws"
            + " an exception and the process is aborted, if true the next file will be processed", defaultValue = "true")
    public boolean skipErrors = true;
    private AbstractStream stream;

    public RecursiveDirectoryStream(SourceURL url) {
        super(url);
    }


    private class GlobVisitor extends SimpleFileVisitor<Path> {

        private final PathMatcher matcher;

        public GlobVisitor(String pattern) {
            matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
            Path name = file.getFileName();
            if (name != null && attr.isRegularFile()) {
                if (matcher.matches(file)) {
                    files.add(file);
                }
            } else {
                log.info("Not a regular file: {} ", file);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            log.error("Could not visit file: {}", file);
            return FileVisitResult.CONTINUE;
        }
    }


    @Override
    public void init() throws Exception {

        if (streams != null && streams.size() > 1) {
            log.error("This multistrream only supports 1 substream");
        }

        Path startingDir = Paths.get(url.getFile());
        GlobVisitor globVisitor = new GlobVisitor(Paths.get(startingDir.toString(), pattern).toString());
        Files.walkFileTree(startingDir, new HashSet<FileVisitOption>(), maxDepth, globVisitor);

        if (files.isEmpty()) {
            log.error("No files could be loaded for pattern {}", pattern);
            throw new RuntimeException("No files could be loaded");
        }

        log.info("Loaded " + files.size() + " files for streaming.");

        if (stream == null && additionOrder != null) {
            stream = (AbstractStream) streams.get(additionOrder.get(0));
            stream.setUrl(new SourceURL(files.poll().toUri().toURL()));
            stream.init();
            log.info("Streaming file: " + stream.getUrl().toString());
            filesCounter++;
        }
    }


    @Override
    public Data readNext() throws Exception {

        try {

            //create new stream when we don't have one
            if (stream == null) {
                if (files.isEmpty()) {
                    return null;
                }
                File f = files.poll().toFile();

                stream = (AbstractStream) streams.get(additionOrder.get(0));
                stream.setUrl(new SourceURL(f.toURI().toURL()));
                stream.init();

                filesCounter++;
            }

            Data data = stream.read();
            if (data == null) {
                //no data was returned
                if (files.isEmpty()) {
                    //no more files to read -> stop the stream
                    return null;
                }
                //get new file
                stream.close();
                File f = files.poll().toFile();

                stream.setUrl(new SourceURL(f.toURI().toURL()));
                stream.init();
                data = stream.readNext();
                filesCounter++;
            }

            return data;

        } catch (IOException e) {
            log.info("File: " + stream.getUrl().toString() + " throws IOException.");

            if (skipErrors) {
                log.info("Skipping broken files. Continuing with next file.");
                stream = null;
                failedFilesCounter++;
                failedFilesList.add(stream.getUrl().toString());
                return this.readNext();
            } else {
                log.error("Stopping stream because of IOException");
                e.printStackTrace();
                stream.close();
                return null;
            }
        }

    }


    @Override
    public void close() throws Exception {
        super.close();
        log.info("In total " + filesCounter + " files were processed.");
        log.info("In total " + failedFilesCounter + " were broken (and therefore skipped).");
        for (String fileName : failedFilesList) {
            log.info(fileName);
        }
    }
}
