package fact.auxservice;

import fact.auxservice.AbstractDrivePointFactory;
import fact.auxservice.drivepoints.DrivePoint;
import fact.auxservice.drivepoints.SourcePoint;
import stream.Data;

public class SourcePointFactory extends AbstractDrivePointFactory {
    @Override
    public DrivePoint createDrivePoint(Data item) throws IllegalArgumentException{
        return new SourcePoint(item);
    }
}