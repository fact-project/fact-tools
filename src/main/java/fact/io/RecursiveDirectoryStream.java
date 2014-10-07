package fact.io;

import com.google.gson.Gson;
import fact.auxservice.AuxFileService;
import stream.Data;
import stream.annotations.Parameter;
import stream.io.AbstractStream;
import stream.io.SourceURL;
import stream.io.multi.AbstractMultiStream;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This will automatically add @drsFile (and @driveFile if path to aux files is provided) keys to the data item.
 * Created by mackaiver on 9/21/14.
 */
public class RecursiveDirectoryStream extends AbstractMultiStream {

    static BlockingQueue<File> files = new LinkedBlockingQueue<>();

    @Parameter(required = false, description = "Maximum depth of folders to traverse", defaultValue = "6")
    private int  maxDepth = 6;
    @Parameter(required = true, description = "The suffix to filter files by. .gz for example.")
    private String suffix;

    private String currentFilePath = "";

    @Parameter(required = false, description = "A file containing a json array of strings with the allowed filenames. "+
            "(excluding the possible suffix)")
    private SourceURL listUrl = null;

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

        HashSet<String> fileNames = new HashSet<>();
        if(listUrl !=  null){
            File list = new File(listUrl.getFile());
            Gson g = new Gson();
            fileNames = g.fromJson(new BufferedReader(new FileReader(list)), new HashSet<String>().getClass());
        }

        log.info("Loading files.");
        ArrayList<File> fileList = walkFiles(f, suffix, 0);
        for (File file: fileList){
            if(fileNames.contains(file.getName())) {
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
//                System.out.println(name);
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
        }
        Data data = stream.read();
        if (data != null) {
            addDrsAndtrackingFileToData(data);
            return data;
        } else {
            File f = files.poll();
            if (f == null)
                return null;
            else {
                stream.close();
                stream.setUrl(new SourceURL(f.toURI().toURL()));
                stream.init();
                data = stream.read();
                addDrsAndtrackingFileToData(data);
                log.info("Streaming file: " + stream.getUrl().toString());
                return data;
            }
        }
    }

    public void addDrsAndtrackingFileToData(Data data) throws FileNotFoundException, URISyntaxException, MalformedURLException {
        if (currentFilePath != data.get("@source").toString()){
            currentFilePath = data.get("@source").toString();
            SourceURL drsFile = findDRSFile(currentFilePath);
            data.put("@drsFile", drsFile);
        }
    }



    /**
     * Tries to automatically find a fitting drs file for the currrent fits file. The methods iterates over all
     * files in the directory containing the substring "drs.fits" and having the same date as the current data
     * fits file. The .drs file with the nearest number lower than the number of the data file will be returned.
     *
     * The expected format of the file names is:
     * 20130331_012.drs.fits*
     * 20130331_013.fits*
     *
     * @param pathToCurrentFitsFile
     * @return
     * @throws java.io.FileNotFoundException
     */
    private SourceURL findDRSFile(String pathToCurrentFitsFile) throws FileNotFoundException {
        try {
            final URI uri = new URI(pathToCurrentFitsFile);
            File currentFile = new File(uri);

            String currentFileName = currentFile.getName();
            if (currentFileName.length() < 17 ){
                throw new FileNotFoundException("filename had the wrong format");
            }
            final int fileNumber = new Integer(currentFileName.substring(9,12));
            final int dateNumber = new Integer(currentFileName.substring(0,8));
            File parent = currentFile.getParentFile();
            if(parent.isDirectory()){
                String[] drsFileNames = parent.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        if(name.contains("drs.fits")) {
                            int drsFileNumber = new Integer(name.substring(9, 12));
                            int drsDateNumber = new Integer(name.substring(0,8));
                            if (drsDateNumber == dateNumber && drsFileNumber <= fileNumber){
                                return true;
                            }
                        }
                        return  false;
                    }
                });
                if(drsFileNames == null){
                    //IO error occurred
                    throw new FileNotFoundException("Could not load file names from directory");
                }
                Arrays.sort(drsFileNames);
                File f = new File(parent,drsFileNames[drsFileNames.length-1]);
                return new SourceURL(f.toURI().toURL());
            }
            return null;
        } catch (Exception e) {
//            e.printStackTrace();
            throw new FileNotFoundException("Could not find DRS file automatically");
        }
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
