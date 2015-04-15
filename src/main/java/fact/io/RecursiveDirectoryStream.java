package fact.io;

import com.google.gson.Gson;
import stream.Data;
import stream.annotations.Parameter;
import stream.io.AbstractStream;
import stream.io.SourceURL;
import stream.io.multi.AbstractMultiStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * Created by mackaiver on 9/21/14.
 */
@Deprecated
public class RecursiveDirectoryStream extends AbstractMultiStream {

    static BlockingQueue<File> files = new LinkedBlockingQueue<>();

    @Parameter(required = false, description = "Maximum depth of folders to traverse", defaultValue = "6")
    private int  maxDepth = 6;
    @Parameter(required = true, description = "The suffix to filter files by. .gz for example.")
    private String suffix;

    @Parameter(required = false, description = "A file containing a json array of strings with the allowed filenames. "+
            "(excluding the possible suffix)")
    private SourceURL listUrl = null;

    //counts how many files have been processed
    private int filesCounter = 0;

    private AbstractStream stream;

    public RecursiveDirectoryStream(SourceURL url) {
        super(url);
    }

    @Override
    public void init() throws Exception {
        if(!files.isEmpty()){
            log.debug("files allready loaded");
            //file allready loaded
            return;
        }
        SourceURL url = getUrl();
        File f = new File(url.getFile());
        if (!f.isDirectory()){
            throw new IllegalArgumentException("Provided url does not point to a directory");
        }

        HashSet<String> fileNamesFromWhiteList = new HashSet<>();
        if(listUrl !=  null){
            File list = new File(listUrl.getFile());
            Gson g = new Gson();
            fileNamesFromWhiteList = g.fromJson(new BufferedReader(new FileReader(list)), new HashSet<String>().getClass());
        }

        log.info("Loading files.");
        ArrayList<File> fileList = walkFiles(f, suffix, 0);
        for (File file: fileList){
            if(fileNamesFromWhiteList.isEmpty() || fileNamesFromWhiteList.contains(file.getName())){
                files.add(file);
            }
        }
        log.info("Loaded " + files.size() + " files for streaming.");
        //super.init();
    }

    /**
     * Recursivly walks over all direcotries below dir. Up to the maximum depth of depth; Returns only files which names
     * end with the given suffix.
     * @param dir
     * @param suffix
     * @param depth
     * @return
     */
    private ArrayList<File> walkFiles(File dir, final String suffix, int depth){
        if(depth > maxDepth){
            return new ArrayList<>();
        }
        //get all files ending with the right suffix in this directory and add them to the queue
        String[] fileNames = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                File f = new File(dir, name);
                if (f.isHidden() || f.isDirectory()){
                    return false;
                }
                if (name.endsWith(suffix)) {
                    return true;
                }
                return false;

            }
        });
        ArrayList<File> l = new ArrayList<>();
        for(String fName : fileNames){
            l.add(new File(dir, fName));
        }
        //now get all subdirs and do it again
        String[] directoryNames = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                File f = new File(dir, name);
                if (!f.isHidden() && f.isDirectory()){
                    return true;
                }
                return false;
            }
        });
        depth++;
        for (String dirName  : directoryNames){
            File subDir = new File(dir, dirName);
            l.addAll(walkFiles(subDir, suffix, depth));
        }
        return l;
    }

    @Override
    public Data readNext() throws Exception {
        if (stream == null) {
            stream = (AbstractStream) streams.get(additionOrder.get(0));
            File f = files.poll();
            if (f == null) {
                return null;
            }
            stream.setUrl(new SourceURL(f.toURI().toURL()));
            log.info("Streaming file: " + stream.getUrl().toString());
            stream.init();
            filesCounter++;
        }
        Data data = stream.read();
        if (data != null) {
            return data;
        } else {
            File f = files.poll();
            if (f == null)
                return null;
            else {
                stream.close();
//                stream.count = 0L;
                stream.setUrl(new SourceURL(f.toURI().toURL()));
                stream.init();
                data = stream.read();
                log.info("Streaming file: " + stream.getUrl().toString());
                filesCounter++;
                return data;
            }
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        log.info("In total " + filesCounter +  " files were processed.");
    }
    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public void setListUrl(SourceURL listUrl) {
        this.listUrl = listUrl;
    }

}
