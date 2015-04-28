package fact.auxservice;

import fact.auxservice.strategies.AuxPointStrategy;
import fact.io.FitsStream;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
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
import java.util.Map;
import java.util.TreeSet;

/**
 * This implements an AuxiliaryService {@link fact.auxservice.AuxiliaryService}  providing data from the auxiliary
 * files written by the telescopes data acquisition system.
 *
 * Given the path to the aux folder, that is the folder containing all the auxiliary file for a <bold>specific night</bold>,
 * via .xml this service will read the requested data and store them in a map of {@link fact.auxservice.AuxPoint}.
 *
 * 79FRbPVCSKsBzn
 * Created by kaibrugge on 07.10.14.
 */
public class AuxFileService implements AuxiliaryService {

    Logger log = LoggerFactory.getLogger(AuxFileService.class);

    Map<AuxiliaryServiceName, TreeSet<AuxPoint>> services = new HashMap<>();

    @Parameter(required = false, description = "The path to the folder containing the auxilary data as .fits files")
    SourceURL auxFolder;

    boolean isInit = false;
    private HashMap<AuxiliaryServiceName, SourceURL> auxFileUrls;

    @Override
    public AuxPoint getAuxiliaryData(AuxiliaryServiceName serviceName, DateTime eventTimeStamp, AuxPointStrategy strategy) throws FileNotFoundException {
        if(!isInit){
            auxFileUrls = findAuxFileUrls(auxFolder);
            isInit = true;
        }
        if(!services.containsKey(serviceName)){
            services.put(serviceName, readDataFromFile(auxFileUrls.get(serviceName)));
        }
        TreeSet<AuxPoint> set = services.get(serviceName);
        return strategy.getPointFromTreeSet(set, eventTimeStamp);
    }


    private TreeSet<AuxPoint>  readDataFromFile(SourceURL driveFileUrl){
        TreeSet<AuxPoint> result = new TreeSet<>();
        FitsStream stream = new FitsStream(driveFileUrl);
        try {
            stream.init();
            Data slowData = stream.readNext();
            while (slowData != null) {
                double time = Double.parseDouble(slowData.get("Time").toString()) * 86400;// + 2440587.5;
                DateTime t = new DateTime((long)time*1000L, DateTimeZone.UTC);
                AuxPoint p = new AuxPoint(t, slowData);
                result.add(p);
//                    mgr.addDrivePoint(factory.createDrivePoint(slowData));
                slowData = stream.readNext();
            }
            stream.close();
            return result;
        } catch (Exception e) {
            log.error("Failed to load data from AUX file: {}", e.getMessage());
            throw new RuntimeException();
        }
    }


    /**
     * Finds all .fits file in the given folder that contain one of the values from AuxiliaryServiceName in their
     * file name. This is public for unit testing purposes.
     * @param auxFolder
     * @return a mapping from a AuxiliaryServiceName to a SourceURL which points to a file.
     * @throws java.io.FileNotFoundException in case the provided URL doesnt point to a readable folder.
     */
    public HashMap<AuxiliaryServiceName, SourceURL> findAuxFileUrls(SourceURL auxFolder) throws FileNotFoundException {

        Path p = Paths.get(auxFolder.getPath());
        File folder = p.toFile();

        if(!folder.isDirectory() || !folder.exists()){
            throw new FileNotFoundException("Could not enter folder. Does it exist?");
        }
        final HashMap<AuxiliaryServiceName, SourceURL> m = new HashMap<>();
        folder.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".fits")) {
                    try {
                        //get name of aux file by removing the date string (first 9 characters) and the file ending
                        String auxName = name.substring(9);
                        auxName = auxName.substring(0, auxName.length() - 5);
                        File f = new File(dir, name);
                        m.put(AuxiliaryServiceName.valueOf(auxName), new SourceURL(f.toURI().toURL()));
                    } catch (MalformedURLException e) {
//                        e.printStackTrace();
                        log.error("Could not create path to auxillary file " + dir + " " +name);
                        return false;
                    }catch (IllegalArgumentException e) {
//                        e.printStackTrace();
                        log.warn("The file " + dir + " " +name + " is not a recognized aux service. ");
                        return false;
                    }
                    return true;
                }
                return false;
            }
        });
        return m;
    }

    @Override
    public void reset() throws Exception {
    }

    public void setAuxFolder(SourceURL auxFolder) {
        this.auxFolder = auxFolder;
    }


}
