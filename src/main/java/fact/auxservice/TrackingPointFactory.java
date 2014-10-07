package fact.auxservice;

import fact.auxservice.drivepoints.DrivePoint;
import fact.auxservice.drivepoints.SourcePoint;
import stream.Data;

public class TrackingPointFactory extends AbstractDrivePointFactory {
    @Override
    public DrivePoint createDrivePoint(Data item) {
        return new SourcePoint(item);
    }
}