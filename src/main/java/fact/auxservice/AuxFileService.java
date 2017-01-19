package fact.auxservice;

import fact.auxservice.strategies.AuxPointStrategy;

import fact.io.hdureader.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
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
 * Created by kaibrugge on 07.10.14.
 */
public class AuxFileService implements AuxiliaryService {

    Logger log = LoggerFactory.getLogger(AuxFileService.class);

    Map<AuxiliaryServiceName, TreeSet<AuxPoint>> services = new HashMap<>();

    @Parameter(required = false, description = "The path to the folder containing the auxilary data as .fits files")
    SourceURL auxFolder;

    boolean isInit = false;
    private HashMap<AuxiliaryServiceName, SourceURL> auxFileUrls;

    /**
     * This method returns an AuxPoint according to the strategy and the time stamp passed to this method.
     * This is useful for getting the source position from the drive files for example. It can work like this:
     *
     *      AuxPoint trackingPoint = auxService.getAuxiliaryData(AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION, timeStamp, closest);
     *      double ra = trackingPoint.getDouble("Ra");
     *
     * @param serviceName The name of the service.
     * @param eventTimeStamp The time stamp of the current raw data event.
     * @param strategy One of the strategies provided.
     * @return
     * @throws FileNotFoundException
     */
    @Override
    public AuxPoint getAuxiliaryData(AuxiliaryServiceName serviceName, DateTime eventTimeStamp, AuxPointStrategy strategy) throws FileNotFoundException {
        if(!isInit){
            auxFileUrls = findAuxFileUrls(auxFolder);
            isInit = true;
        }
        if(!services.containsKey(serviceName)){
            services.put(serviceName, readDataFromFile(auxFileUrls.get(serviceName), serviceName.toString()));
        }
        TreeSet<AuxPoint> set = services.get(serviceName);

        DateTime firstTimeStamp = set.first().getTimeStamp();
        DateTime lastTimeStamp = set.last().getTimeStamp();
        if(firstTimeStamp.isAfter(eventTimeStamp) || lastTimeStamp.isBefore(eventTimeStamp))
        {
            log.warn("Provided event timestamp not in auxiliary File.");
        }

        //TODO: load a new file in case we need stuff from the next day or night. I don't know whether this is ever a valid use case.
        return strategy.getPointFromTreeSet(set, eventTimeStamp);
    }


    /**
     * Reads data from a file provided by the url and creates an AuxPoint for each event in the file.
     * @param auxFileUrl url to the auxfile
     * @return treeset containing auxpoints ordered by their timestamp
     */
    private TreeSet<AuxPoint>  readDataFromFile(SourceURL auxFileUrl, String extname) {
        TreeSet<AuxPoint> result = new TreeSet<>();

        //create a fits object
        try {
            URL url = new URL(auxFileUrl.getProtocol(), auxFileUrl.getHost(), auxFileUrl.getPort(), auxFileUrl.getFile());
            FITS fits = new FITS(url);
            BinTable auxDataBinTable = fits.getBinTableByName(extname)
                                           .orElseThrow(
                                               () -> new RuntimeException("BinTable '" + extname + "' not in aux file")
                                           );
            BinTableReader auxDataBinTableReader = BinTableReader.forBinTable(auxDataBinTable);

            while (auxDataBinTableReader.hasNext()) {
                OptionalTypesMap<String, Serializable> auxData = auxDataBinTableReader.getNextRow();

                auxData.getDouble("Time").ifPresent(time -> {
                    DateTime t = new DateTime((long) (time * 24 * 60 * 60 * 1000), DateTimeZone.UTC);
                    AuxPoint p = new AuxPoint(t, auxData);
                    result.add(p);
                });
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to load data from AUX file: {}", e.getMessage());
            throw new RuntimeException(e);
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

        if(!folder.exists()){
            throw new FileNotFoundException("The path does not exist:  " + folder.toString());
        }
        if(!folder.isDirectory()){
            throw new FileNotFoundException("The path does not point to a directory:  " + folder.toString());
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
                        log.error("Could not create path to auxillary file " + dir + " " +name);
                        return false;
                    }catch (IllegalArgumentException e) {
                        log.warn("The file " + dir + "/" +name + " is not a recognized aux service. ");
                        return false;
                    }catch (IndexOutOfBoundsException e){
                        log.warn("The file " + dir + "/" +name + " is not a recognized aux service. " +
                                 "Could not parse file name into a recognized service.");
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
