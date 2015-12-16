package fact.io;

import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.annotations.Parameter;
import stream.io.AbstractStream;
import stream.io.SourceURL;
import stream.io.multi.AbstractMultiStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * Created by mackaiver on 12/15/15.
 */
public class RecursiveDirectoryStream extends AbstractMultiStream {
    static Logger log = LoggerFactory.getLogger(RecursiveDirectoryStream.class);


    public BlockingQueue<Path> files = new LinkedBlockingQueue<>();

    @Parameter(required = false, description = "Maximum depth of folders to traverse", defaultValue = "6")
    private int  maxDepth = 6;

    @Parameter(required = true, description = "The pattern to filter files by. Understand usual glob syntax")
    private String pattern;

    //counts how many files have been processed
    private int filesCounter = 0;
    private int failedFilesCounter = 0;
    private List<String> failedFilesList = new ArrayList<>();
    @Parameter(required = false, description = "If false the reading of a broken file throws"
    		+ " an exception and the process is aborted, if true the next file will be processed", defaultValue = "false")
    private boolean skipBrokenFiles = false;
    private AbstractStream stream;

    public RecursiveDirectoryStream(SourceURL url) {
        super(url);
    }


    private class GlobVisitor extends SimpleFileVisitor<Path>{

        private final PathMatcher matcher;

        public GlobVisitor(String pattern) {
            Path startingDir = Paths.get(url.getFile());
            String globPattern = "glob:" + startingDir + pattern;
            matcher = FileSystems.getDefault().getPathMatcher(globPattern);
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
            Path name = file.getFileName();
            if (name != null && attr.isRegularFile()) {

                if (matcher.matches(file)){
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

//        @Override
//        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//            Path name = dir.getFileName();
//            if(name != null && matcher.matches(name)){
//                return FileVisitResult.CONTINUE;
//            } else{
//                return FileVisitResult.SKIP_SUBTREE;
//            }
//        }
    }


    @Override
    public void init() throws Exception {

        if (streams != null && streams.size() > 1){
            log.error("This multistrream only supports 1 substream");
        }

        Path startingDir = Paths.get(url.getFile());
        GlobVisitor globVisitor = new GlobVisitor(pattern);
        Files.walkFileTree(startingDir, new HashSet<FileVisitOption>(), maxDepth, globVisitor);

        if(files.isEmpty()){
            log.info("No files could be loaded");
            return;
        }

        log.info("Loaded " + files.size() + " files for streaming.");
        //super.init();

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

        Data data = stream.read();
        if (data != null) {
            return data;
        }

        if (files.isEmpty()) {
            //stop the stream
            return null;
        }
        //stream from new file.
        File f = files.poll().toFile();

        stream.close();
        stream.setUrl(new SourceURL(f.toURI().toURL()));

        try {
            log.info("Streaming file: " + stream.getUrl().toString());
            stream.init();
            data = stream.read();
            if ( data == null){
                //try next file;
                return this.readNext();
            } else {
                return data;
            }

        } catch (IOException e) {
            log.info("File: " + stream.getUrl().toString() + " throws IOException.");

            if (skipBrokenFiles) {
                log.info("Skipping broken files. Continuing with next file.");
                failedFilesCounter++;
                failedFilesList.add(stream.getUrl().toString());
                return this.readNext();
            } else {
                log.error("Stopping stream because of IOException");
                e.printStackTrace();
                return null;
            }

        }
    }



    @Override
    public void close() throws Exception {
        super.close();
        log.info("In total " + filesCounter +  " files were processed.");
        log.info("In total " + failedFilesCounter + " were broken (and therefore skipped). Filenames:");
        for ( String fileName : failedFilesList)
        {
        	log.info(fileName);
        }
    }
    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

	public void setSkipBrokenFiles(boolean skipBrokenFiles) {
		this.skipBrokenFiles = skipBrokenFiles;
	}

}
