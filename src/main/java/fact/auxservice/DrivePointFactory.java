package fact.auxservice;

import fact.auxservice.drivepoints.DrivePoint;
import stream.Data;

/**
 * CONGRTATULATIONS! TYPER ERASURE IS TEH FAIL
 * USE ABSTRACTGENERICFACTORY WOOHOOOOO!
 * Created by kaibrugge on 07.10.14.
 */
public class DrivePointFactory<T extends DrivePoint> {

    Class<T> clazz;
    public DrivePointFactory(final Class<T> clazz){
        this.clazz = clazz;
    }

    public T createDrivePoint(Data item) throws IllegalArgumentException {
        try {
            T drivePoint = clazz.newInstance();
            drivePoint.initialiseWithDataItem(item);
            return clazz.newInstance();

        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    };
}
