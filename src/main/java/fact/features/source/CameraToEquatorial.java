package fact.features.source;

import fact.Constants;
import fact.Utils;
import fact.coordinates.CameraCoordinate;
import fact.coordinates.EarthLocation;
import fact.coordinates.EquatorialCoordinate;
import fact.coordinates.HorizontalCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.time.ZonedDateTime;

/**
 * Created by maxnoe on 20.06.17.
 */
public class CameraToEquatorial implements Processor {


    private static final Logger log = LoggerFactory.getLogger(CameraToEquatorial.class);

    @Parameter
    public String cameraCoordinateKey = null;

    @Parameter
    public String pointingPositionKey = null;

    @Parameter
    public String outputKey = null;

    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item,"timestamp", ZonedDateTime.class);
        ZonedDateTime timeStamp = (ZonedDateTime) item.get("timestamp");
        Utils.isKeyValid(item, cameraCoordinateKey, CameraCoordinate.class);
        Utils.isKeyValid(item, pointingPositionKey, HorizontalCoordinate.class);

        CameraCoordinate cameraCoordinate = (CameraCoordinate) item.get(cameraCoordinateKey);
        HorizontalCoordinate pointingPosition = (HorizontalCoordinate) item.get(pointingPositionKey);

        HorizontalCoordinate cameraCoordinateHorizontal = cameraCoordinate.toHorizontal(pointingPosition, Constants.FOCAL_LENGTH_MM);
        EquatorialCoordinate cameraCoordinateEquatorial = cameraCoordinateHorizontal.toEquatorial(timeStamp, EarthLocation.FACT);

        item.put(outputKey, cameraCoordinateEquatorial);
        item.put(outputKey + "RaHA", cameraCoordinateEquatorial.getRightAscensionHA());
        item.put(outputKey + "DecDeg", cameraCoordinateEquatorial.getDeclinationDeg());
        return item;
    }
}
