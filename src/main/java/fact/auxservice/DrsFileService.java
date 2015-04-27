package fact.auxservice;

import stream.io.SourceURL;
import stream.service.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.Arrays;

/**
 * Implements a service to automaticaly find a drs file belonging to the current data file.
 * Created by kaibrugge on 09.10.14.
 */
@Deprecated
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
        if (currentFileName.length() < 17 ){
            throw new IllegalArgumentException();
        }
        return new Integer(currentFileName.substring(0,8));
    }

    public boolean isDrsFileBelowFileNumber(File dir, String name, int dateNumber, int fileNumber) {
        File f = new File(dir, name);
        if(name.contains("drs.fits")) {
            try {
                int drsFileNumber = getFileNumberFromFile(f);
                int drsDateNumber = getDateNumberFromFile(f);
                if (drsDateNumber == dateNumber && drsFileNumber <= fileNumber) {
                    return true;
                }
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return  false;
    }


    public String[] getDrsFilesInFolder(File folder, File currentFile) throws FileNotFoundException{
        final int dateNumber = getDateNumberFromFile(currentFile);
        final int fileNumber = getFileNumberFromFile(currentFile);
        String[] drsFileNames = folder.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return isDrsFileBelowFileNumber(dir, name, dateNumber, fileNumber);
            }
        });
        if(drsFileNames == null){
            //IO error occurred
            throw new FileNotFoundException("Could not load file names from directory");
        }
        Arrays.sort(drsFileNames);
        return drsFileNames;
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

            String[] drsFileNames = getDrsFilesInFolder(parent, currentFile);

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
