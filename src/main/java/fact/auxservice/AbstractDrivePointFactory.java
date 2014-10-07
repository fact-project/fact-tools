package fact.auxservice;

import fact.auxservice.drivepoints.DrivePoint;
import stream.Data;

/**
 * Created by kaibrugge on 07.10.14.
 */
public abstract class AbstractDrivePointFactory {

    public abstract DrivePoint createDrivePoint(Data item) throws IllegalArgumentException;
}
