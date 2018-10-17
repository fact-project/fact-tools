package fact.starservice;

import fact.Constants;
import fact.Utils;
import fact.auxservice.AuxPoint;
import fact.auxservice.AuxiliaryService;
import fact.auxservice.AuxiliaryServiceName;
import fact.auxservice.strategies.AuxPointStrategy;
import fact.auxservice.strategies.Closest;
import fact.coordinates.CameraCoordinate;
import fact.coordinates.EarthLocation;
import fact.coordinates.EquatorialCoordinate;
import fact.coordinates.HorizontalCoordinate;
import fact.hexmap.ui.overlays.SourcePositionOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.annotations.Service;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * Find bright stars in the field of view and add their positions in the camera to the data item
 *
 * @author Maximilian Noethe &lt;maximilian.noethe@tu-dortmund.de&gt;
 */

public class StarsInFOV implements Processor {

    @Parameter(required = true, description = "Output key for the resulting CameraCoordinate[] of star positions")
    public String outputKey = null;

    @Service(description = "Name of the service that provides aux files")
    public AuxiliaryService auxService;

    @Parameter(description = "The key containing the event timestamp")
    public String timeStampKey = "timestamp";

    @Parameter(description = "Maximum magnitude to consider")
    public double maxMagnitude = 4.0;

    private final AuxPointStrategy closest = new Closest();

    @Service(required = false)
    private final StarService starService = new StarService();

    /**
     * This process finds stars in the field of view and adds them to the data item
     */
    @Override
    public Data process(Data item) {

        EquatorialCoordinate pointingEquatorial;
        HorizontalCoordinate pointingHorizontal;

        // Assume observations
        Utils.isKeyValid(item, timeStampKey, ZonedDateTime.class);
        ZonedDateTime timeStamp = (ZonedDateTime) item.get(timeStampKey);

        AuxPoint trackingPoint;
        try {
            // We want to get the tracking point which is closest to the current event.
            trackingPoint = auxService.getAuxiliaryData(AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION, timeStamp, closest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        pointingEquatorial = EquatorialCoordinate.fromHourAngleAndDegrees(
                trackingPoint.getDouble("Ra"), trackingPoint.getDouble("Dec")
        );

        pointingHorizontal = pointingEquatorial.toHorizontal(timeStamp, EarthLocation.FACT);

        Star[] starsInFOV = starService.getStarsInFov(pointingEquatorial, maxMagnitude);

        CameraCoordinate[] starPositions = new CameraCoordinate[starsInFOV.length];

        for (int i = 0; i < starsInFOV.length; i++) {
            Star star = starsInFOV[i];
            HorizontalCoordinate horizontal = star.equatorialCoordinate.toHorizontal(timeStamp, EarthLocation.FACT);
            starPositions[i] = horizontal.toCamera(pointingHorizontal, Constants.FOCAL_LENGTH_MM);
            item.put(outputKey + "_" + i, new SourcePositionOverlay(star.name, starPositions[i]));
        }

        item.put(outputKey, starPositions);

        return item;
    }
}
