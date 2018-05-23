package fact.features.source;

import fact.Constants;
import fact.Utils;
import fact.auxservice.AuxPoint;
import fact.auxservice.AuxiliaryService;
import fact.auxservice.AuxiliaryServiceName;
import fact.auxservice.strategies.AuxPointStrategy;
import fact.auxservice.strategies.Closest;
import fact.auxservice.strategies.Earlier;
import fact.coordinates.CameraCoordinate;
import fact.coordinates.EarthLocation;
import fact.coordinates.EquatorialCoordinate;
import fact.coordinates.HorizontalCoordinate;
import fact.hexmap.ui.overlays.SourcePositionOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.annotations.Service;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * This calculates the position of the source in the camera. The Telescope usually does not look
 * directly at the source but somewhere close by. That means the image of the source projected by the mirrors onto
 * the camera is not exactly in the center but at some point (X,Y). This point will be called source position from now on.
 * The point (0.0, 0.0) is the center of the camera.
 * In  order to calculate the source position we need to know where the telescope is looking. And at what time exactly.
 * This data is written by the telescope drive system into  auxiliary .fits files called DRIVE_CONTROL_SOURCE_POSITION and
 * DRIVE_CONTROL_TRACKING_POSITION.
 * <p>
 * This processor handles a handful of different tasks. It can calculate the source position in the camera for
 * some fixed celestial coordinates (e.g. In case you want to get the coordinates of a star projected onto the camera plane)
 * <p>
 * For data processing we need the auxService to read data from both the DRIVE_CONTROL_SOURCE_POSITION and the DRIVE_CONTROL_TRACKING_POSITION
 * files. The first contains the name and celestial coordinates of the source we're at looking while the second contains
 * information at where the telescope pointing which is updated in small intervals.
 * <p>
 * Unfortunately MC processed files have to be treated differently than data files since there are no pointing positions written
 * to auxiliary files. For newer ceres versions which allow the simulation of wobble positions (after revision 18159),
 * the source and pointing information are simply taken from the data stream.
 * <p>
 * For older ceres versions you can simply specify fixed X and Y coordinates in the camera plane.
 *
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt; , Fabian Temme &lt;fabian.temme@tu-dortmund.de&gt, Max Nöthe;
 */
public class SourcePosition implements StatefulProcessor {
    private static final Logger log = LoggerFactory.getLogger(SourcePosition.class);


    @Parameter(required = true, description = "The key to the sourcepos array that will be written to the map.")
    public String outputKey = null;

    @Service(required = false, description = "Name of the service that provides aux files")
    public AuxiliaryService auxService;

    @Parameter(required = false, description = "The key containing the event timestamp")
    public String timeStampKey = "timestamp";

    @Parameter(description = "If set, the fixed x position of the source in mm")
    public Double x = null;

    @Parameter(description = "If set, the fixed y position of the source in mm")
    public Double y = null;

    @Parameter(description = "In case of MC-Input you specify the key to the source coordinates")
    public String sourceZdKey = null;

    @Parameter(description = "In case of MC-Input you specify the key to the source coordinates")
    public String sourceAzKey = null;

    @Parameter(description = "In case of MC-Input you specify the key to the pointing coordinates")
    public String pointingZdKey = null;

    @Parameter(description = "In case of MC-Input you specify the key to the pointing coordinates")
    public String pointingAzKey = null;

    // used in case we need the sourceposition of a star in the camera
    @Parameter(required = false)
    public Double sourceRightAscension = null;

    @Parameter(required = false)
    public Double sourceDeclination = null;

    private final AuxPointStrategy closest = new Closest();
    private final AuxPointStrategy earlier = new Earlier();

    //flag which indicates whether were are looking at montecarlo files which have a wobble position
    public boolean hasMcWobblePosition;

    /**
     * The process method adds the azimuth and zenith values for the pointing, tracking and source position.
     * It also adds an overlay to the item so the position can be displayed in the viewer.
     */
    @Override
    public Data process(Data item) {
        EquatorialCoordinate sourceEquatorial;
        HorizontalCoordinate sourceHorizontal;
        CameraCoordinate sourceCamera;

        EquatorialCoordinate pointingEquatorial;
        HorizontalCoordinate pointingHorizontal;
        HorizontalCoordinate auxPointingHorizontal;

        // In case the source position is fixed. Used for older ceres version <= revision 18159
        if (x != null && y != null) {

            // add source position to data item
            sourceCamera = new CameraCoordinate(x, y);
            auxPointingHorizontal = HorizontalCoordinate.fromDegrees(0, 0);
            pointingHorizontal = HorizontalCoordinate.fromDegrees(0, 0);
            sourceHorizontal = HorizontalCoordinate.fromDegrees(0, 0);

        } else if (hasMcWobblePosition) {
            double pointingZd = Utils.valueToDouble(item.get(pointingZdKey));
            double pointingAz = Utils.valueToDouble(item.get(pointingAzKey));
            double sourceZd = Utils.valueToDouble(item.get(sourceZdKey));
            double sourceAz = Utils.valueToDouble(item.get(sourceAzKey));
            // Due to the fact, that Ceres handle the coordinate in a different way, we have to
            // rotate the coordinate system by 180 deg such that 0 deg is north
            pointingAz = 180 + pointingAz;
            sourceAz = 180 + sourceAz;

            pointingHorizontal = HorizontalCoordinate.fromDegrees(pointingZd, pointingAz);
            sourceHorizontal = HorizontalCoordinate.fromDegrees(sourceZd, sourceAz);

            // Now we can calculate the source position from the zd,az coordinates for pointing and source
            sourceCamera = sourceHorizontal.toCamera(pointingHorizontal, Constants.FOCAL_LENGTH_MM);

            pointingHorizontal = HorizontalCoordinate.fromDegrees(pointingZd, pointingAz);
            auxPointingHorizontal = pointingHorizontal;

        } else {
            // Assume observations
            Utils.isKeyValid(item, timeStampKey, ZonedDateTime.class);
            ZonedDateTime timeStamp = (ZonedDateTime) item.get(timeStampKey);


            // the source position is not updated very often. We have to get the point from the auxfile which
            // was written earlier to the current event
            AuxPoint sourcePoint;
            AuxPoint trackingPoint;
            try {
                sourcePoint = auxService.getAuxiliaryData(AuxiliaryServiceName.DRIVE_CONTROL_SOURCE_POSITION, timeStamp, earlier);

                //We want to get the tracking point which is closest to the current event.
                trackingPoint = auxService.getAuxiliaryData(AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION, timeStamp, closest);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            pointingEquatorial = EquatorialCoordinate.fromHourAngleAndDegrees(
                    trackingPoint.getDouble("Ra"), trackingPoint.getDouble("Dec")
            );

            pointingHorizontal = pointingEquatorial.toHorizontal(timeStamp, EarthLocation.FACT);

            if (sourceDeclination != null && sourceRightAscension != null) {
                sourceEquatorial = EquatorialCoordinate.fromHourAngleAndDegrees(sourceRightAscension, sourceDeclination);
            } else {
                sourceEquatorial = EquatorialCoordinate.fromHourAngleAndDegrees(
                        sourcePoint.getDouble("Ra_src"),
                        sourcePoint.getDouble("Dec_src")
                );
            }

            sourceHorizontal = sourceEquatorial.toHorizontal(timeStamp, EarthLocation.FACT);
            sourceCamera = sourceHorizontal.toCamera(pointingHorizontal, Constants.FOCAL_LENGTH_MM);

            String sourceName = sourcePoint.getString("Name");
            item.put("sourceName", sourceName);

            Double auxZd = trackingPoint.getDouble("Zd");
            Double auxAz = trackingPoint.getDouble("Az");

            auxPointingHorizontal = HorizontalCoordinate.fromDegrees(auxZd, auxAz);
        }

        item.put(outputKey, sourceCamera);
        item.put(outputKey + "_x", sourceCamera.xMM);
        item.put(outputKey + "_y", sourceCamera.yMM);

        item.put("aux_pointing_position", auxPointingHorizontal);
        item.put("pointing_position", pointingHorizontal);
        item.put("source_position_horizontal", sourceHorizontal);

        item.put("source_position_zd", sourceHorizontal.getZenithDeg());
        item.put("source_position_az", sourceHorizontal.getAzimuthDeg());

        item.put("aux_pointing_position_zd", auxPointingHorizontal.getZenithDeg());
        item.put("aux_pointing_position_az", auxPointingHorizontal.getAzimuthDeg());

        item.put("pointing_position_zd", pointingHorizontal.getZenithDeg());
        item.put("pointing_position_az", pointingHorizontal.getAzimuthDeg());

        item.put(outputKey + "Marker", new SourcePositionOverlay(outputKey, sourceCamera));

        return item;
    }


    /**
     * Here we check whether an auxservice has been set or some fixed coordinates have been provided in the .xml.
     * If any of the parameters sourceZdKey,sourceAzKey,pointingZdKey,pointingAzKey are set then all need to be set.
     */
    @Override
    public void init(ProcessContext arg0) throws Exception {
        if (x != null && y != null) {
            log.warn("Setting source position to dummy values X: " + x + "  Y: " + y);
            return;
        }

        hasMcWobblePosition = false;
        if (!(sourceZdKey == null && sourceAzKey == null && pointingZdKey == null && pointingAzKey == null)) {
            if (sourceZdKey != null && sourceAzKey != null && pointingZdKey != null && pointingAzKey != null) {
                hasMcWobblePosition = true;
                log.warn("Using zd and az values from the data item");
            } else {
                log.error("You need to specify all position keys (sourceZdKey,sourceAzKey,pointingZdKey,pointingAzKey");
                throw new IllegalArgumentException();
            }
        } else if (auxService == null) {

            log.error("You have to provide fixed sourceposition coordinates X and Y, or specify position keys, or specify the auxService.");
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void resetState() throws Exception {
    }


    @Override
    public void finish() throws Exception {
    }


}
