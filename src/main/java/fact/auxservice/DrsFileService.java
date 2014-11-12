package fact.auxservice;

import stream.io.SourceURL;
import stream.service.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.Arrays;

/**
 * Implements a service to automaticly find a drs file belonging to the current data file.
 * Created by kaibrugge on 09.10.14.
 */
public class DrsFileService implements Service {

    String currentDataFile = "";
    SourceURL currentDrsFile;

    public int getFileNumberFromFile(File f) throws IllegalArgumentException {
        String currentFileName = f.getName();
        if (f.length() < 17 ){
            throw new IllegalArgumentException();
        }
        return new Integer(currentFileName.substring(9,12));
    }

    public int getDateNumberFromFile(File f) throws IllegalArgumentException {
        String currentFileName = f.getName();
        if (f.length() < 17 ){
            throw new IllegalArgumentException();
        }
        return new Integer(currentFileName.substring(0,8));
    }


    public String[] getDrsFileInFolder(File folder, File currentFile) throws FileNotFoundException{
        final int dateNumber = getDateNumberFromFile(currentFile);
        final int fileNumber = getFileNumberFromFile(currentFile);
        String[] drsFileNames = folder.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                File f = new File(dir, name);
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
    public SourceURL findDRSFile(String pathToCurrentFitsFile) throws FileNotFoundException {
        if(currentDataFile.equals(pathToCurrentFitsFile)){
            return currentDrsFile;
        }
        try {
            final URI uri = new URI(pathToCurrentFitsFile);
            File currentFile = new File(uri);
            File parent = currentFile.getParentFile();

            String[] drsFileNames = getDrsFileInFolder(parent, currentFile);

            File f = new File(parent,drsFileNames[drsFileNames.length-1]);
            currentDrsFile = new SourceURL(f.toURI().toURL());
            return currentDrsFile;

        } catch (Exception e) {
//            e.printStackTrace();
            throw new FileNotFoundException("Could not find DRS file automatically");
        }
    }

    @Override
    public void reset() throws Exception {

    }
}
