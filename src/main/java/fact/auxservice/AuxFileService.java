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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.MalformedURLException;

/**
 * Created by kaibrugge on 07.10.14.
 */
public class AuxFileService implements FileService {

    Logger log = LoggerFactory.getLogger(AuxFileService.class);

    DrivePointManager<TrackingPoint> trackingPointManager = new DrivePointManager();
    DrivePointManager<SourcePoint> sourcePointManager = new DrivePointManager();

    File currentFile;

    @Parameter(required = false, description = "The path to the folder containing the auxilary fits files. " +
            "This folder should contain the usual folder structure: year/month/day/<aux_files>")
    SourceURL auxFolder;
    @Parameter(required = false, description = "The path to the DRIVE_CONTROL_TRACKING_POSITION file.")
    SourceURL trackingFile;
    @Parameter(required = false, description = "The path to the DRIVE_CONTROL_SOURCE_POSITION file")
    SourceURL sourceFile;

    public synchronized SourcePoint getDriveSourcePosition(File dataFile, double julianday){
        if(!dataFile.equals(currentFile)){
            currentFile = dataFile;
            setNewFilePath(currentFile);
        }
        return sourcePointManager.getPoint(julianday);
    }
    public synchronized TrackingPoint getDriveTrackingPosition(File dataFile, double julianday){
        if(!dataFile.equals(currentFile)){
            currentFile = dataFile;
            setNewFilePath(currentFile);
        }
        return trackingPointManager.getPoint(julianday);
    }

    private void setNewFilePath(File currentPath) {
        try {

            String dateString = getDateStringFromPath(currentPath);

            if(auxFolder != null) {
                SourceURL sourcePosUrl = findAuxFileByName(auxFolder, dateString, "DRIVE_CONTROL_SOURCE_POSITION.fits");
                SourceURL trackingUrl = findAuxFileByName(auxFolder, dateString, "DRIVE_CONTROL_TRACKING_POSITION.fits");
                loadPointsFromFile(sourcePosUrl, sourcePointManager, new SourcePointFactory());
                loadPointsFromFile(trackingUrl, trackingPointManager, new TrackingPointFactory());
            } else if(sourceFile !=  null && trackingFile != null){
                loadPointsFromFile(sourceFile, sourcePointManager, new SourcePointFactory());
                loadPointsFromFile(trackingFile, trackingPointManager, new TrackingPointFactory());
            } else{
                log.error("Neither auxFolder nor filePaths are provided.");
                throw new IllegalArgumentException("Neither auxFolder nor filePaths are provided.");
            }

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
    private SourceURL findAuxFileByName(SourceURL auxFolder,final String dateString,  final String auxFileName) throws FileNotFoundException {

        String year = dateString.substring(0,4);
        String month = dateString.substring(4,6);
        String day = dateString.substring(6,8);
        File folder = new File(auxFolder.getFile(),year);
        folder = new File(folder, month);
        folder = new File(folder, day);
        if(!folder.isDirectory() || !folder.exists()){
            throw new FileNotFoundException("Could not build path for tracking file.");
        }
        String[] trackingfiles = folder.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(name.contains(dateString) &&  name.contains(auxFileName)){
                    return true;
                }
                return false;
            }
        });
        if(trackingfiles.length != 1){
            throw new FileNotFoundException("Could not find tracking file");
        }
        File driveFile = new File(folder, trackingfiles[0]);
        try {
            return new SourceURL(driveFile.toURI().toURL());
        } catch (MalformedURLException e) {
            //TODO: Fitstream sohuld have a file constructor
            log.error("This should never happen ");
            return null;
        }
    }

    public void loadPointsFromFile(SourceURL driveFileUrl, DrivePointManager mgr, AbstractDrivePointFactory factory){
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
