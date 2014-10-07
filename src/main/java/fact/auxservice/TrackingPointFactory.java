package fact.auxservice;

import fact.auxservice.drivepoints.DrivePoint;
import fact.auxservice.drivepoints.SourcePoint;
import fact.auxservice.drivepoints.TrackingPoint;
import stream.Data;

public class TrackingPointFactory extends AbstractDrivePointFactory {
    @Override
    public DrivePoint createDrivePoint(Data item) throws IllegalArgumentException
    {
        return new TrackingPoint(item);
    }
}