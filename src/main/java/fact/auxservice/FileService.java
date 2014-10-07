package fact.auxservice;

import fact.auxservice.drivepoints.TrackingPoint;
import stream.io.SourceURL;
import stream.service.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

/**
 * Created by kaibrugge on 07.10.14.
 */
public interface FileService extends Service {
    public TrackingPoint getDriveTrackingPosition(File dataFile, double julianday);
}
