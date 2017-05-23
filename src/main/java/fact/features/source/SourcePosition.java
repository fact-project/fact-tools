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
import fact.coordinates.EquatorialCoordinate;
import fact.coordinates.HorizontalCoordinate;
import fact.hexmap.ui.overlays.SourcePositionOverlay;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.annotations.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;


/**
 *  This calculates the position of the source in the camera. The Telescope usually does not look
 *  directly at the source but somewhere close by. That means the image of the source projected by the mirrors onto
 *  the camera is not exactly in the center but at some point (X,Y). This point will be called source position from now on.
 *  The point (0.0, 0.0) is the center of the camera.
 *  In  order to calculate the source position we need to know where the telescope is looking. And at what time exactly.
 *  This data is written by the telescope drive system into  auxiliary .fits files called DRIVE_CONTROL_SOURCE_POSITION and
 *  DRIVE_CONTROL_TRACKING_POSITION.
 *
 *  This processor handles a handful of different tasks. It can calculate the source position in the camera for
 *  some fixed celestial coordinates (e.g. In case you want to get the coordinates of a star projected onto the camera plane)
 *
 *  For data processing we need the auxService to read data from both the DRIVE_CONTROL_SOURCE_POSITION and the DRIVE_CONTROL_TRACKING_POSITION
 *  files. The first contains the name and celestial coordinates of the source we're at looking while the second contains
 *  information at where the telescope pointing which is updated in small intervals.
 *
 *  Unfortunately MC processed files have to be treated differently than data files since there are no pointing positions written
 *  to auxiliary files. For newer ceres versions which allow the simulation of wobble positions (after revision 18159),
 *  the source and pointing information are simply taken from the data stream.
 *
 *  For older ceres versions you can simply specify fixed X and Y coordinates in the camera plane.
 *
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt; , Fabian Temme &lt;fabian.temme@tu-dortmund.de&gt, Max Nöthe;
 */
public class SourcePosition implements StatefulProcessor {
    private static final Logger log = LoggerFactory.getLogger(SourcePositionOld.class);


    @Parameter(required = true, description = "The key to the sourcepos array that will be written to the map.")
    private String outputKey = null;

    public void setAuxService(AuxiliaryService auxService) {
        this.auxService = auxService;
    }

    @Service(required = false, description = "Name of the service that provides aux files")
    private AuxiliaryService auxService;

    @Parameter(required = false)
    private Double x = null;
    @Parameter(required = false)
    private Double y = null;

    //TODO Standards setzen?
    @Parameter(required = false, description = "In case of MC-Input you specify the key to the source coordinates")
    private String sourceZdKey = null;
    @Parameter(required = false, description = "In case of MC-Input you specify the key to the source coordinates")
    private String sourceAzKey = null;
    @Parameter(required = false, description = "In case of MC-Input you specify the key to the pointing coordinates")
    private String pointingZdKey = null;
    @Parameter(required = false, description = "In case of MC-Input you specify the key to the pointing coordinates")
    private String pointingAzKey = null;

    //flag which indicates whether were are looking at montecarlo files which have a wobble position
    public boolean hasMcWobblePosition;


    //used in case we need the sourceposition of a star in the camera
    @Parameter(required = false)
    private Double sourceRightAscension = null;
    @Parameter(required = false)
    private Double sourceDeclination = null;

    private final AuxPointStrategy closest = new Closest();
    private final AuxPointStrategy earlier = new Earlier();

    @Override
    public void finish() throws Exception {
    }

    /**
     * Here we check whether an auxservice has been set or some fixed coordinates have been provided in the .xml.
     * If any of the parameters sourceZdKey,sourceAzKey,pointingZdKey,pointingAzKey are set then all need to be set.
     */
    @Override
    public void init(ProcessContext arg0) throws Exception {
        if(x !=  null && y != null){
            log.warn("Setting source position to dummy values X: " + x + "  Y: " + y);
            return;
        }

        hasMcWobblePosition = false;
        if ( !(sourceZdKey == null && sourceAzKey == null && pointingZdKey == null && pointingAzKey == null) ){
        	if (sourceZdKey != null && sourceAzKey != null && pointingZdKey != null && pointingAzKey != null)
        	{
                hasMcWobblePosition = true;
        		log.warn("Using zd and az values from the data item");
        	}
        	else
        	{
        		log.error("You need to specify all position keys (sourceZdKey,sourceAzKey,pointingZdKey,pointingAzKey");
        		throw new IllegalArgumentException();
        	}
        } else if (auxService == null ){

            log.error("You have to provide fixed sourceposition coordinates X and Y, or specify position keys, or specify the auxService.");
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void resetState() throws Exception {
    }


    /**
     * The process method adds the azimuth and zenith values for the pointing, tracking and source position.
     * It also adds an overlay to the item so the position can be displayed in the viewer.
     */
    @Override
    public Data process(Data data) {
        EquatorialCoordinate sourceEquatorial;
        HorizontalCoordinate sourceHorizontal;
        CameraCoordinate sourceCamera;

        EquatorialCoordinate pointingEquatorial;
        HorizontalCoordinate pointingHorizontal;

        // In case the source position is fixed. Used for older ceres version <= revision 18159
        if(x != null && y !=  null) {
            // add source position to data item
            sourceCamera = new CameraCoordinate(x, y);
            data.put("@sourceOverlay" + outputKey, new SourcePositionOverlay(outputKey, sourceCamera));
            data.put(outputKey, sourceCamera);

            data.put("AzTracking", 0);
            data.put("ZdTracking", 0);

            data.put("AzPointing", 0);
            data.put("ZdPointing", 0);

            data.put("AzSourceCalc", 0);
            data.put("ZdSourceCalc", 0);
            return data;
        }

        if (hasMcWobblePosition)
        {
        	double pointingZd = Utils.valueToDouble(data.get(pointingZdKey));
        	double pointingAz = Utils.valueToDouble(data.get(pointingAzKey));
        	double sourceZd = Utils.valueToDouble(data.get(sourceZdKey));
        	double sourceAz = Utils.valueToDouble(data.get(sourceAzKey));
        	// Due to the fact, that Ceres handle the coordinate in a different way, we have to
        	// rotate the coordinate system by 180 deg such that 0 deg is north
        	pointingAz = 180 + pointingAz;
        	sourceAz = 180 + sourceAz;

        	pointingHorizontal = HorizontalCoordinate.fromDegrees(pointingZd, pointingAz);
            sourceHorizontal = HorizontalCoordinate.fromDegrees(sourceZd, sourceAz);

        	// Now we can calculate the source position from the zd,az coordinates for pointing and source
        	sourceCamera = sourceHorizontal.toCamera(pointingHorizontal, Constants.focalLength);
        	data.put(outputKey, sourceCamera);

            data.put("AzTracking", pointingAz);
            data.put("ZdTracking", pointingZd);

            data.put("AzPointing", pointingAz);
            data.put("ZdPointing", pointingZd);

            data.put("AzSourceCalc", sourceAz);
            data.put("ZdSourceCalc", sourceZd);

            data.put("@sourceOverlay" + outputKey, new SourcePositionOverlay(outputKey, sourceCamera));
            return data;
        }

        try {
            int[] unixTimeUTC = (int[]) data.get("UnixTimeUTC");
            if(unixTimeUTC == null){
                log.error("The key \"UnixTimeUTC\" was not found in the event. Ignoring event");
                return null;
            }

            Instant unixTime = Instant.ofEpochSecond(unixTimeUTC[0], 1000 * unixTimeUTC[1]);
            ZonedDateTime timeStamp = ZonedDateTime.ofInstant(unixTime, ZoneOffset.UTC);
            DateTime jodaTimeStamp = new DateTime(unixTime.toEpochMilli(), DateTimeZone.UTC);

            // the source position is not updated very often. We have to get the point from the auxfile which
            // was written earlier to the current event
            AuxPoint sourcePoint = auxService.getAuxiliaryData(AuxiliaryServiceName.DRIVE_CONTROL_SOURCE_POSITION, jodaTimeStamp, earlier);

            //We want to get the tracking point which is closest to the current event.
            AuxPoint trackingPoint = auxService.getAuxiliaryData(AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION, jodaTimeStamp, closest);

            pointingEquatorial = EquatorialCoordinate.fromHourAngleAndDegrees(
                    trackingPoint.getDouble("Ra"), trackingPoint.getDouble("Dec")
            );

            pointingHorizontal = pointingEquatorial.toHorizontal(timeStamp, Constants.FACTLocation);

            if (sourceDeclination != null && sourceRightAscension != null)
            {
                sourceEquatorial = EquatorialCoordinate.fromHourAngleAndDegrees(sourceRightAscension, sourceDeclination);
            } else {
                sourceEquatorial = EquatorialCoordinate.fromHourAngleAndDegrees(
                        sourcePoint.getDouble("Ra_src"),
                        sourcePoint.getDouble("Dec_src")
                );
            }

            sourceHorizontal = sourceEquatorial.toHorizontal(timeStamp, Constants.FACTLocation);
            sourceCamera = sourceHorizontal.toCamera(pointingHorizontal, Constants.focalLength);

            String sourceName = sourcePoint.getString("Name");
            data.put("SourceName", sourceName);
            data.put(outputKey, sourceCamera);

            data.put("AzTracking", trackingPoint.getDouble("Az"));
            data.put("ZdTracking", trackingPoint.getDouble("Zd"));

            data.put("AzPointing", pointingHorizontal.getAzimuthDeg());
            data.put("ZdPointing", pointingHorizontal.getZenithDeg());

            data.put("AzSourceCalc", sourceHorizontal.getAzimuthDeg());
            data.put("ZdSourceCalc", sourceHorizontal.getZenithDeg());

            data.put("@Source" + outputKey, new SourcePositionOverlay(outputKey, sourceCamera));

        } catch (IOException e) {
            log.error("SourcePositionOld could not be calculated. Stopping stream.");
            e.printStackTrace();
            return null;
        }

        return data;
    }

    public void setX(Double x) {
        this.x = x;
    }
    public void setY(Double y) {
        this.y = y;
    }

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public void setSourceZdKey(String sourceZdKey) {
		this.sourceZdKey = sourceZdKey;
	}

	public void setSourceAzKey(String sourceAzKey) {
		this.sourceAzKey = sourceAzKey;
	}

	public void setPointingZdKey(String pointingZdKey) {
		this.pointingZdKey = pointingZdKey;
	}

	public void setPointingAzKey(String pointingAzKey) {
		this.pointingAzKey = pointingAzKey;
	}

	public void setSourceRightAscension(Double sourceRightAscension) {
		this.sourceRightAscension = sourceRightAscension;
	}

	public void setSourceDeclination(Double sourceDeclination) {
		this.sourceDeclination = sourceDeclination;
	}

}
