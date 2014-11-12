package fact.auxservice;

import fact.auxservice.drivepoints.DrivePointManager;
import fact.auxservice.drivepoints.SourcePoint;
import fact.auxservice.drivepoints.TrackingPoint;
import fact.io.FitsStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.annotations.Parameter;
import stream.io.SourceURL;
import stream.service.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.util.HashMap;

/**
 * This service should provide some data from the auxilary files for a given data file.
 *
 * Each datafile can have a different aux file. Given the data file we can request a PointManager for the corresponding
 * aux file.
 * A point manager can return rows from the aux file..
 * Created by kaibrugge on 07.10.14.
 */
public class AuxFileService implements Service {

    Logger log = LoggerFactory.getLogger(AuxFileService.class);

    DrivePointManager<TrackingPoint> trackingPointManager = new DrivePointManager();
    DrivePointManager<SourcePoint> sourcePointManager = new DrivePointManager();


    @Parameter(required = false, description = "The path to the folder containing the auxilary fits files. " +
            "This folder should contain the usual folder structure: year/month/day/<aux_files>")
    SourceURL auxFolder;
    @Parameter(required = false, description = "The path to the DRIVE_CONTROL_TRACKING_POSITION file.")
    SourceURL trackingFile;
    @Parameter(required = false, description = "The path to the DRIVE_CONTROL_SOURCE_POSITION file")
    SourceURL sourceFile;

    public synchronized DrivePointManager<TrackingPoint> getTrackingPointManager(File dataFile){
        setNewFilePath(dataFile);
        return trackingPointManager;
    }

    public synchronized DrivePointManager<SourcePoint> getSourcePointManager(File dataFile){
        setNewFilePath(dataFile);
        return sourcePointManager;
    }

    private void setNewFilePath(File currentPath) {
        try {
            String dateString = getDateStringFromPath(currentPath);
            if(auxFolder != null) {
                HashMap<String, SourceURL> m = findAuxFileUrls(auxFolder, dateString);
                sourceFile = m.get("DRIVE_CONTROL_SOURCE_POSITION");
                trackingFile = m.get("DRIVE_CONTROL_TRACKING_POSITION");
            }
            sourcePointManager = getPointManagerFromFile(sourceFile, new SourcePointFactory());
            trackingPointManager = getPointManagerFromFile(trackingFile, new TrackingPointFactory());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getDateStringFromPath(File currentFile) throws FileNotFoundException {

        String currentFileName = currentFile.getName();
        if (currentFileName.length() < 17 ){
            throw new FileNotFoundException("Filename had the wrong format");
        }
        return currentFileName.substring(0,8);
    }

    /**
     * Goes to the folder provided by auxfolder. Then uses the datestring to select the right year, month and day
     * @param auxFolder
     * @return
     * @throws java.io.FileNotFoundException
     * @throws java.net.MalformedURLException
     */
    private HashMap<String, SourceURL> findAuxFileUrls(SourceURL auxFolder, final String dateString) throws FileNotFoundException {

        String year = dateString.substring(0,4);
        String month = dateString.substring(4,6);
        String day = dateString.substring(6,8);
        File folder = new File(auxFolder.getFile(),year);
        folder = new File(folder, month);
        folder = new File(folder, day);
        if(!folder.isDirectory() || !folder.exists()){
            throw new FileNotFoundException("Could not build path for tracking file.");
        }
        final HashMap<String, SourceURL> m = new HashMap<>();
        folder.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.contains(dateString) && name.endsWith(".fits")) {
                    try {
                        //get name of aux file by removing the date string (first 9 cahrachters) and the file ending
                        String auxName = name.substring(9);
                        auxName = auxName.substring(0, auxName.length() - 5);
                        File f = new File(dir, name);
                        m.put(auxName, new SourceURL(f.toURI().toURL()));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                return false;
            }
        });
        return m;
    }

    private DrivePointManager getPointManagerFromFile(SourceURL driveFileUrl, AbstractDrivePointFactory factory){
        DrivePointManager mgr = new DrivePointManager();
        FitsStream stream = new FitsStream(driveFileUrl);
        try {
            stream.init();
            Data slowData = stream.readNext();
            while (slowData != null) {
                try {
                    mgr.addDrivePoint(factory.createDrivePoint(slowData));
                } catch(IllegalArgumentException a){
                    log.warn(a.getLocalizedMessage() + " In file: " + driveFileUrl.toString());
                }
                slowData = stream.readNext();
            }
            stream.close();
            return mgr;
        } catch (Exception e) {
            log.error("Failed to load data from AUX file: {}", e.getMessage());
            throw new RuntimeException();
        }
    }

    @Override
    public void reset() throws Exception {
    }

    public void setTrackingFile(SourceURL trackingFile) {
        this.trackingFile = trackingFile;
    }

    public void setSourceFile(SourceURL sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void setAuxFolder(SourceURL auxFolder) {
        this.auxFolder = auxFolder;
    }

}
