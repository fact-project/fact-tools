package fact.auxservice;

import fact.auxservice.drivepoints.DrivePoint;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * This service should provide some data from the auxiliary files for a given data file.
 * Given the path to the aux folder via .xml and a data file this service will find and store the urls to the
 * corresponding auxiliary files.
 *
 * The urls are stored in a map that returns the url provided the **name** of the file. The name is defined to be
 * the substring of the filename that does not contain the extension (.fits) or the datestring. Valid names are:
 *
 *  * DRIVE_CONTROL_TRACKING_POSITION
 *  * DRIVE_CONTROL_SOURCE_POSITION
 *  * TEMPERATURE_MESSAGE
 *  * TNG_WEATHER_DUST
 *  * etc...
 *
 * A processor can  request a DrivePointManager from the service.
 *
 * 79FRbPVCSKsBzn
 * Created by kaibrugge on 07.10.14.
 */
public class AuxFileService implements Service {

    Logger log = LoggerFactory.getLogger(AuxFileService.class);


    File currentlyMappedDataFile = new File("");
    HashMap<String, SourceURL> auxFileMap =  new HashMap<>() ;

    @Parameter(required = false, description = "The path to the folder containing the auxilary fits files. " +
            "This folder should contain the usual folder structure: year/month/day/<aux_files>")
    SourceURL auxFolder;


    /**
     * Get the DrivePointManager for the DRIVE_CONTROL_TRACKING_POSITION file.
     * @param dataFile The .fits data file that stream is currently working on.
     * @return The DrivePointManager containing the data from the DRIVE_CONTROL_TRACKING_POSITION file.
     */
    public synchronized DrivePointManager<TrackingPoint> getTrackingPointManager(File dataFile){
        DrivePointManager<TrackingPoint> dM = (DrivePointManager<TrackingPoint>) getPointManagerForDataFile(dataFile, "DRIVE_CONTROL_TRACKING_POSITION", new DrivePointFactory<>(TrackingPoint.class));
        return dM;
    }

    public synchronized DrivePointManager<TrackingPoint> getTrackingPointManagerForSourceFile(SourceURL sourceFile){
        DrivePointManager<TrackingPoint> dM = (DrivePointManager<TrackingPoint>) getPointManagerFromFile(sourceFile, new DrivePointFactory<>(TrackingPoint.class));
        return dM;
    }

    /**
     * Get the DrivePointManager for the DRIVE_CONTROL_SOURCE_POSITION file.
     * @param dataFile The .fits data file that stream is currently working on.
     * @return The DrivePointManager containing the data from the DRIVE_CONTROL_SOURCE_POSITION file.
     */
    public synchronized DrivePointManager<SourcePoint> getSourcePointManager(File dataFile){
        DrivePointManager<SourcePoint> dM = (DrivePointManager<SourcePoint>) getPointManagerForDataFile(dataFile, "DRIVE_CONTROL_SOURCE_POSITION", new DrivePointFactory<>(SourcePoint.class));
        return dM;
    }

    public synchronized DrivePointManager<SourcePoint> getSourcePointManagerForSourceFile(SourceURL sourceFile){
        DrivePointManager<SourcePoint> dM = (DrivePointManager<SourcePoint>) getPointManagerFromFile(sourceFile, new DrivePointFactory<>(SourcePoint.class));
        return dM;
    }


    private  DrivePointManager<? extends DrivePoint>  getPointManagerForDataFile(File currentFile, String name, DrivePointFactory factory) {

        try {
            //lets check if we need to update the map.
            if(!currentFile.equals(currentlyMappedDataFile)){
                String dateString = getDateStringFromFile(currentFile);
                auxFileMap = findAuxFileUrls(auxFolder, dateString);
            }
            return getPointManagerFromFile(auxFileMap.get(name), factory);
        } catch (FileNotFoundException e) {
            log.error("Aux file not found.");
            e.printStackTrace();
            return null;
        }
    }

    private String getDateStringFromFile(File currentFile) throws FileNotFoundException {

        String currentFileName = currentFile.getName();
        if (currentFileName.length() < 17 ){
            throw new FileNotFoundException("Filename had the wrong format");
        }
        return currentFileName.substring(0,8);
    }



    /**
     * Goes to the folder provided by auxfolder. Then uses the datestring to select the right year, month and day for
     * subfolders.
     * @param auxFolder
     * @return
     * @throws java.io.FileNotFoundException
     */
    public HashMap<String, SourceURL> findAuxFileUrls(SourceURL auxFolder, final String dateString) throws FileNotFoundException {

        String year = dateString.substring(0,4);
        String month = dateString.substring(4,6);
        String day = dateString.substring(6,8);

        Path p = Paths.get(auxFolder.getPath(), year, month, day);
        File folder = p.toFile();

        if(!folder.isDirectory() || !folder.exists()){
            throw new FileNotFoundException("Could not build path for tracking file. Expected name of the data file should have the format YYYYMMDD_ID");
        }
        final HashMap<String, SourceURL> m = new HashMap<>();
        folder.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.contains(dateString) && name.endsWith(".fits")) {
                    try {
                        //get name of aux file by removing the date string (first 9 characters) and the file ending
                        String auxName = name.substring(9);
                        auxName = auxName.substring(0, auxName.length() - 5);
                        File f = new File(dir, name);
                        m.put(auxName, new SourceURL(f.toURI().toURL()));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        log.error("Could not create path to auxillary file " + dir + " " +name);
                        return false;
                    }
                    return true;
                }
                return false;
            }
        });
        return m;
    }

    private DrivePointManager<? extends DrivePoint> getPointManagerFromFile(SourceURL driveFileUrl, DrivePointFactory factory){
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

    public void setAuxFolder(SourceURL auxFolder) {
        this.auxFolder = auxFolder;
    }

}
