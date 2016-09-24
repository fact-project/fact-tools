package fact.features.source;


import fact.auxservice.AuxPoint;
import fact.auxservice.AuxiliaryService;
import fact.auxservice.AuxiliaryServiceName;
import fact.auxservice.strategies.AuxPointStrategy;
import fact.auxservice.strategies.Closest;
import fact.auxservice.strategies.Earlier;
import fact.hexmap.ui.overlays.SourcePositionOverlay;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import fact.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.annotations.Service;

import java.io.IOException;


/**
 *  This calculates the position of the source in the camera. The Telescope usually does not look
 *  directly at the source but somewhere close by. That means the image of the source projected by the mirrors onto
 *  the camera is not exactly in the center but at some point (X,Y). This point will be called source position from now on.
 *  The point (0.0, 0.0) is the center of the camera.
 *  In  order to calculate the source position we need to know where the telescope is looking. And at what time exactly.
 *  This data is written by the telescope drive system into  auxilary .fits files called DRIVE_CONTROL_SOURCE_POSITION and
 *  DRIVE_CONTROL_TRACKING_POSITION.
 *
 *  This processor handles a handful of different tasks. It can calculate the sourceposition in the camera for
 *  some fixed celestial coordinates (e.g. In case you want to get the coordinates of a star projected onto the camera plane)
 *
 *  For data processing we need the auxService to read data from both the DRIVE_CONTROL_SOURCE_POSITION and the DRIVE_CONTROL_TRACKING_POSITION
 *  files. The first contains the name and celestial coordinates of the source we're at looking while the second contains
 *  information at where the telescope pointing which is updated in small intervals.
 *
 *  Unfortunately MC processed files have to be treated differently than data files since there are no pointing positions written
 *  to auxiliary files. For newer ceres versions which allow the simulation of wobble positions (after revision 18159),
 *  the source and pointing information are simply taken from the datastream.
 *
 *  For older ceres versions you can simply specify fixed X and Y coordinates in the camera plane.
 *
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt; , Fabian Temme &lt;fabian.temme@tu-dortmund.de&gt, Max Nöthe;
 */
public class SourcePosition implements StatefulProcessor {
    static Logger log = LoggerFactory.getLogger(SourcePosition.class);


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


    AuxPointStrategy closest = new Closest();
    AuxPointStrategy earlier = new Earlier();

    //position of the Telescope
    private final double telescopeLongitudeDeg = -17.890701389;
    private final double telescopeLatitudeDeg = 28.761795;
    //Distance from earth center
    private final double distanceToEarthCenter = 4889.0;

    // reference datetime
    DateTime gstReferenceDateTime = new DateTime(2000, 1, 1, 12, 0, DateTimeZone.UTC);


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

        // In case the source position is fixed. Used for older ceres version <= revision 18159
        if(x != null && y !=  null) {
            // add source position to data item
            double[] source = {x, y};
            data.put("@sourceOverlay" + outputKey, new SourcePositionOverlay(outputKey, source));
            data.put(outputKey, source);
            
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
        	// Now we can calculate the source position from the zd,az coordinates for pointing and source
        	double[] sourcePosition = getSourcePosition(pointingAz, pointingZd, sourceAz, sourceZd);
        	data.put(outputKey, sourcePosition);
        	
            data.put("AzTracking", pointingAz);
            data.put("ZdTracking", pointingZd);

            data.put("AzPointing", pointingAz);
            data.put("ZdPointing", pointingZd);

            data.put("AzSourceCalc", sourceAz);
            data.put("ZdSourceCalc", sourceZd);

            data.put("@sourceOverlay" + outputKey, new SourcePositionOverlay(outputKey, sourcePosition));
            return data;
        }

        try {
            int[] eventTime = (int[]) data.get("UnixTimeUTC");
            if(eventTime == null){
                log.error("The key \"UnixTimeUTC\" was not found in the event. Ignoring event");
                return null;
            }

            double unixTime = eventTime[0] + (eventTime[1] / 1000000.0);
            DateTime timeStamp = new DateTime((long) (1000 * unixTime), DateTimeZone.UTC);

            // the source position is not updated very often. We have to get the point from the auxfile which
            // was written earlier to the current event
            AuxPoint sourcePoint = auxService.getAuxiliaryData(AuxiliaryServiceName.DRIVE_CONTROL_SOURCE_POSITION, timeStamp, earlier);

            //We want to get the tracking point which is closest to the current event.
            AuxPoint trackingPoint = auxService.getAuxiliaryData(AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION, timeStamp, closest);

            double ra = trackingPoint.getDouble("Ra");
            double dec = trackingPoint.getDouble("Dec");

            //convert celestial coordinates to local coordinate system.
            double[] pointingAzZd = equatorialToHorizontal(ra, dec, timeStamp);

            double[] sourceAzZd;
            if (sourceDeclination != null && sourceRightAscension != null)
            {
                sourceAzZd = equatorialToHorizontal(sourceRightAscension, sourceDeclination, timeStamp);
            } else {
                sourceAzZd = equatorialToHorizontal(
                        sourcePoint.getDouble("Ra_src"),
                        sourcePoint.getDouble("Dec_src"),
                        timeStamp
                );
            }

            double[] sourcePosition = getSourcePosition(pointingAzZd[0], pointingAzZd[1], sourceAzZd[0], sourceAzZd[1]);

            String sourceName = sourcePoint.getString("Name");
            data.put("SourceName", sourceName);
            data.put(outputKey, sourcePosition);

            data.put("AzTracking", trackingPoint.getDouble("Az"));
            data.put("ZdTracking", trackingPoint.getDouble("Zd"));

            data.put("AzPointing", pointingAzZd[0]);
            data.put("ZdPointing", pointingAzZd[1]);

            data.put("AzSourceCalc", sourceAzZd[0]);
            data.put("ZdSourceCalc", sourceAzZd[1]);

            data.put("@Source" + outputKey, new SourcePositionOverlay(outputKey, sourcePosition));

        } catch (IOException e) {
            log.error("SourcePosition could not be calculated. Stopping stream.");
            e.printStackTrace();
            throw new RuntimeException();
        }

        return data;
    }

    /**
     * Convert a DateTime object to greenwhich sidereal time according to 
     * https://en.wikipedia.org/wiki/Sidereal_time#Definition
     * @param datetime 
     * @return gst in radians
     */
    public double datetimeToGST(DateTime datetime){

    	Duration difference = new Duration(gstReferenceDateTime, datetime);
    	double gst = 18.697374558 + 24.06570982441908 * (difference.getMillis() / 86400000.0);

        // normalize to [0, 24] and convert to radians
        gst = (gst % 24) / 12.0 * Math.PI;
        return gst;
    }

    /**
     * Implementation of the formulas from 
     * https://en.wikipedia.org/wiki/Celestial_coordinate_system#Equatorial_.E2.86.90.E2.86.92_horizontal
     * 
     * @param ra in decimal arc hours (e.g. 5h and 30 minutes : ra = 5.5)
     * @param dec in decimal degrees (e.g. 21 degrees and 30 arc minutes : zd = 21.5)
     * @param datetime DateTime of the event
     * @return an array of length 2 containing {azimuth, zenith} in degree, not null;
     */
    public double[] equatorialToHorizontal(double ra, double dec, DateTime datetime){
        if (ra >= 24.0 || ra < 0.0 || dec >= 360.0 || dec < 0 ){
            throw new RuntimeException("Ra or Dec values are invalid. They should be given in decimal arc hours and decimal degree");
        }

        double gst = datetimeToGST(datetime);

        ra = ra / 12. * Math.PI;
        dec = Math.toRadians(dec);
        
        double telLatRad = Math.toRadians(telescopeLatitudeDeg);
        double telLonRad = Math.toRadians(telescopeLongitudeDeg);

        // wikipedia assumes longitude positive in west direction
        double hourAngle = gst + telLonRad - ra;
        
        double altitude = Math.asin(
                Math.sin(telLatRad) * Math.sin(dec) +
                Math.cos(telLatRad) * Math.cos(dec) * Math.cos(hourAngle)
        );

        double azimuth = Math.atan2(
                Math.sin(hourAngle),
        		Math.cos(hourAngle) * Math.sin(telLatRad) - Math.tan(dec) * Math.cos(telLatRad)
        );

        azimuth -= Math.PI;

        if (azimuth <= - Math.PI){
            azimuth += 2 * Math.PI;
        }
        
        return new double[]{Math.toDegrees(azimuth), 90 - Math.toDegrees(altitude)};
    }



    /**
     * Returns position of the source in the camera in [mm] from the given pointing Azimuth and Zenith
     * Code by F. Temme
     * @param pointingAz
     * @param pointingZd
     * @param sourceAz
     * @param sourceZd
     * @return
     */
    public double[] getSourcePosition(double pointingAz, double pointingZd, double sourceAz, double sourceZd)
    {

        double paz = Math.toRadians(pointingAz);
        double pzd = Math.toRadians(pointingZd);
        double saz = Math.toRadians(sourceAz);
        double szd = Math.toRadians(sourceZd);


        double x = Math.sin(szd) * Math.cos(saz);
        double y = Math.sin(szd) * Math.sin(saz);
        double z = Math.cos(szd);


        double x_rot = -Math.sin(-pzd) * z - Math.cos(-pzd) * (Math.cos(-paz) * x - Math.sin(-paz) * y);
        double y_rot =  Math.sin(-paz) * x + Math.cos(-paz) * y;
        double z_rot =  Math.cos(-pzd) * z - Math.sin(-pzd) * (Math.cos(-paz) * x - Math.sin(-paz) * y);

        double[] r = new double[2];
        r[0] = x_rot * (-distanceToEarthCenter) / z_rot;
        r[1] = - y_rot * (-distanceToEarthCenter) / z_rot;

        return r;
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
