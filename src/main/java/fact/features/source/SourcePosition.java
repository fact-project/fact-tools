package fact.features.source;

import fact.Utils;
import fact.auxservice.AuxFileService;
import fact.auxservice.drivepoints.DrivePointManager;
import fact.auxservice.drivepoints.SourcePoint;
import fact.auxservice.drivepoints.TrackingPoint;
import fact.hexmap.ui.overlays.SourcePositionOverlay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import java.io.File;
import java.util.Date;

/**
 *  This is supposed to calculate the position of the source in the camera. The Telescope usually does not look
 *  directly at the source but somewhere close by. That means the image of the source projected by the mirrors onto
 *  the camera is not exactly in the center but at some point (X,Y). This point will be called source position from now on.
 *  The point (0.0, 0.0) is the center of the camera.
 *  In  order to calculate the source position we need to know where the telescope is looking. And at what time exactly.
 *  This data is written by the telescope drive system into an auxilary .fits file called DRIVE_TRACKING_POSITION.
 *
 *  The azimuth given in the TRACKING file is defined differently -(Az + 180) = calculated Az
 *
 *  TODO: Plot deviation between calculated and written Az and Zd for files in whitelist
 *  TODO: handle ceta tauri and similar cases. (at the moment it feels hacky)
 *  TODO: Receive the different outputKeys from the xml files 
 *  TODO: compare sourcepositions with ganymed
 *
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt; , Fabian Temme &lt;fabian.temme@tu-dortmund.de&gt;
 */
public class SourcePosition implements StatefulProcessor {
    static Logger log = LoggerFactory.getLogger(SourcePosition.class);


    @Parameter(required = true, description = "The key to the sourcepos array that will be written to the map.")
    private String outputKey = null;

    @Parameter(required = false, description = "Name of the service that provides aux files")
    private AuxFileService auxService;

    @Parameter(required = false)
    private SourceURL trackingFileUrl = null;
    @Parameter(required = false)
    private SourceURL sourceFileUrl = null;

    @Parameter(required = false)
    private Double x = null;
    @Parameter(required = false)
    private Double y = null;
    
    @Parameter(required = false)
    private String sourceZdKey = null;
    @Parameter(required = false)
    private String sourceAzKey = null;
    @Parameter(required = false)
    private String pointingZdKey = null;
    @Parameter(required = false)
    private String pointingAzKey = null;
    
    @Parameter(required = false)
    private Double sourceRightAscension = null;
    @Parameter(required = false)
    private Double sourceDeclination = null;

    //position of the Telescope
    public final double telescopeLongitude = -17.890701389;
    public final double telescopeLatitude = 28.761795;
    //Distance from earth center
    public final double distanceToEarthCenter = 4890.0;


    private DrivePointManager<TrackingPoint> trackingManager;
    private DrivePointManager<SourcePoint> sourceManager;
    private File currentFile;

    @Override
    public void finish() throws Exception {
    }

    /**
     * In the init method we read the complete TRACKING_POSITION file and save the values in the locList.
     * For the calculation of the appropriate sky coordinates (that is Azimuth and Zenith) we only need the values "Time", "Ra" and "Dec".
     * There are also values for "Az" and "Zd" in file. These are calculated by the drive system itself. They can be used for a sanity check.
     * These values differ by what seems to be a constant amount in both Az and Zd. About 1 to 3 degrees for the files that I checked.
     * The time unit in the TRACKING file is given in unixtime/86400.0. Its still called MJD for some reason.
     *
     */
    @Override
    public void init(ProcessContext arg0) throws Exception {
        if((trackingFileUrl != null && sourceFileUrl == null) || (sourceFileUrl != null &&trackingFileUrl == null)){
            log.error("You need to specify both trackingFileUrl and sourceFileUrl");
            throw new IllegalArgumentException();
        }
        if(x !=  null && y != null){

            log.warn("Setting sourcepostion to dummy values X: " + x + "  Y: " + y);

        } else if (sourceZdKey != null || sourceAzKey != null || pointingZdKey != null || pointingAzKey != null){
        	if (sourceZdKey != null && sourceAzKey != null && pointingZdKey != null && pointingAzKey != null)
        	{
        		log.warn("Using zd and az values from the data item");
        	}
        	else
        	{
        		log.error("You need to specify all position keys (sourceZdKey,sourceAzKey,pointingZdKey,pointingAzKey");
        		throw new IllegalArgumentException();
        	}
        } else if (auxService == null && trackingFileUrl == null){

            log.error("You have to provide fixed sourceposition coordinates X and Y, or specify position keys, or specify the auxService, or provide sourceFileUrl and trackingFileUrl");
            throw new IllegalArgumentException();

        } else if(trackingFileUrl !=  null && sourceFileUrl != null && auxService == null){
            auxService = new AuxFileService();
            trackingManager = auxService.getTrackingPointManagerForSourceFile(trackingFileUrl);
            sourceManager = auxService.getSourcePointManagerForSourceFile(sourceFileUrl);
        }

    }

    @Override
    public void resetState() throws Exception {
    }

    /**
     * Takes unixTime in seconds and returns the julianday as double
     * @param unixTime
     */
    public double unixTimeToJulianDay(int unixTime){
        return unixTime/86400.0 +  40587.0+2400000.5;
    }

    /**
     * Calculates the Greenwhich Mean Sidereal Time from the julianDay.
     * @param julianDay
     * @return gmst in degrees.
     */
    public double julianDayToGmst(double julianDay) throws IllegalArgumentException {

        if(julianDay <= 2451544.5 ){
            throw new IllegalArgumentException("Dates before 1.1.2000 are not supported");
        }
        double timeAtMidnightBefore = Math.floor(julianDay-0.5) + 0.5;
        double timeOfDay = julianDay - timeAtMidnightBefore;

        double T = (timeAtMidnightBefore - 2451545.0)/36525.0;

        double gmst0Hours = (24110.54841 + 8640184.812866*T + 0.093104*Math.pow(T,2) - 0.0000062*Math.pow(T,3))/3600;

        gmst0Hours %= 24;
        double gmst = gmst0Hours + 1.00273790935*timeOfDay*24;
        if (gmst >= 24)
            gmst = gmst - 24;
        if (gmst < 0)
            gmst = gmst + 24;
        //convert to degrees.
        return (gmst*Math.PI/12);
    }


    /**
     * The unixtimestamp in the data file is saved as an array with two elements. {seconds, miroseconds} it is
     * unclear to me what to do with the second one. I simply used the sum of both in seconds..
     * Even though the numbers are small enough to NOT make a difference anyways.
     * After reading the EventTime from the data we check which datapoint from the slowcontroll file we have to use by
     * comparing the times. We use the point closest in time to the current dataitem.
     *
     * @return data. The dataItem containing the calculated sourcePostion as a double[] of length 2. {x,y} .
     * 				 --Also the deviation between the calculated pointing and the onw written in the .fits TRACKING file.
     */
    @Override
    public Data process(Data data) {
        if(x != null && y !=  null){
            //add source position to dataitem
            double[] source = {x, y};
//			System.out.println("x: "+  source[0] + " y: " +source[1] );
            data.put("@sourceOverlay" + outputKey, new SourcePositionOverlay(outputKey, source));
            data.put(outputKey, source);
            
            data.put("@AzTracking", 0);
            data.put("@ZdTracking", 0);

            data.put("@AzPointing", 0);
            data.put("@ZdPointing", 0);

            data.put("@AzSourceCalc", 0);
            data.put("@ZdSourceCalc", 0);
            return data;
        }
        
        if (sourceZdKey != null || sourceAzKey != null || pointingZdKey != null || pointingAzKey != null)
        {
        	double pointingZd = Utils.valueToDouble(data.get(pointingZdKey));
        	double pointingAz = Utils.valueToDouble(data.get(pointingAzKey));
        	double sourceZd = Utils.valueToDouble(data.get(sourceZdKey));
        	double sourceAz = Utils.valueToDouble(data.get(sourceAzKey));
        	double[] sourcePosition = getSourcePosition(pointingAz, pointingZd, sourceAz, sourceZd);
        	data.put(outputKey, sourcePosition);
        	
            data.put("@AzTracking", pointingAz);
            data.put("@ZdTracking", pointingZd);

            data.put("@AzPointing", pointingAz);
            data.put("@ZdPointing", pointingZd);

            data.put("@AzSourceCalc", sourceAz);
            data.put("@ZdSourceCalc", sourceZd);

            data.put("@sourceOverlay" + outputKey, new SourcePositionOverlay(outputKey, sourcePosition));
            return data;
        }

        int[] eventTime = (int[]) data.get("UnixTimeUTC");
        if(eventTime == null){
            log.error("The key \"UnixTimeUTC \" was not found in the event. Ignoring event");
            return null;
        }
        try {

            File f  = new File(data.get("@source").toString());
            if (!f.equals(currentFile) && trackingFileUrl == null){
//                System.out.println("Requesting new aux file!");
                currentFile = f;
                trackingManager = auxService.getTrackingPointManager(currentFile);
                sourceManager = auxService.getSourcePointManager(currentFile);
            }

            //TODO check this.
            int timestamp = (int) ((eventTime[0]) + (eventTime[1]) / 1000000.0);
            //convert unixtime to julianday
            double julianDay = unixTimeToJulianDay(timestamp);
            //convert julianday to gmst
            double gmst = julianDayToGmst(julianDay);

            TrackingPoint trackingPoint = trackingManager.getPoint(julianDay);
            SourcePoint sourcePoint;
            if (sourceDeclination != null && sourceRightAscension != null)
            {
            	sourcePoint = new SourcePoint();
            	sourcePoint.decSrc = sourceDeclination;
            	sourcePoint.raSrc = sourceRightAscension;
            }
            else
            {
            	sourcePoint = sourceManager.getPoint(julianDay);
            }
                        
            //convert celestial coordinates to local coordinate system.
            double[] pointingAzZd = getAzZd(trackingPoint.ra, trackingPoint.dec, gmst);
            //pointAzDz should be equal to the az dz written by the drive
            //double dev = Math.abs(point.Az - pointingAzDe[0]);
            double[] sourceAzZd = getAzZd(sourcePoint.raSrc, sourcePoint.decSrc, gmst);
            double[] sourcePosition = getSourcePosition(pointingAzZd[0], pointingAzZd[1], sourceAzZd[0], sourceAzZd[1]);

            //add source position to dataitem
            double[] source = {sourcePosition[0], sourcePosition[1]};
            data.put(outputKey, source);
            data.put("@TimeStamp", new Date((long)timestamp*1000L));
            data.put("@AzTracking", trackingPoint.Az);
            data.put("@ZdTracking", trackingPoint.Zd);

            data.put("@AzPointing", pointingAzZd[0]);
            data.put("@ZdPointing", pointingAzZd[1]);

            data.put("@AzSourceCalc", sourceAzZd[0]);
            data.put("@ZdSourceCalc", sourceAzZd[1]);

            data.put("@sourceOverlay" + outputKey, new SourcePositionOverlay(outputKey, source));

        } catch (IllegalArgumentException e){
            log.error("Ignoring event.  " + e.getLocalizedMessage());
            return null;
        }
        return data;
    }

    /**
     * This is an adaption of the C++ Code by F.Temme.  This method calculates Azimuth and Zenith from right ascension,
     * declination and the time in gmst format.
     * @param ra in degrees
     * @param dec in degrees
     * @param gmst the Eventtime of the current event in gmst format
     * @return an array of length 2 containing {azimuth, zenith}, not null;
     */
    public double[] getAzZd(double ra, double dec, double gmst){
        double phi              =  ra / 180.0 * Math.PI;
        double theta            =  (90 - dec) / 180.0 * Math.PI;

        double x                =Math.sin(theta) *Math.cos(phi);
        double y                =Math.sin(theta) *Math.sin(phi);
        double z                =Math.cos(theta);

        double phi_rot_angle    = gmst + (telescopeLongitude / 180.0 * Math.PI);
        double theta_rot_angle  = (telescopeLatitude - 90) / 180.0 * Math.PI;

        double m_yx             = -Math.sin(phi_rot_angle);
        double m_yy             =  Math.cos(phi_rot_angle);

        double m_zx             = -Math.sin(theta_rot_angle) *Math.cos(phi_rot_angle);
        double m_zy             = -Math.sin(theta_rot_angle) *Math.sin(phi_rot_angle);
        double m_zz             =  Math.cos(theta_rot_angle);


        //		    double x_rot            = m_xx * x + m_xy * y + m_xz * z;
        double y_rot            = m_yx * x + m_yy * y;
        double z_rot            = m_zx * x + m_zy * y + m_zz * z;


        double theta_rot        = Math.acos(z_rot);
        double phi_rot          = Math.asin( y_rot /Math.sin(theta_rot) );
        //azimuth and zenith
        double[] r =  {phi_rot / Math.PI * 180.0, theta_rot / Math.PI * 180.0 };
        return r;
    }

    /**
     * Returns position of the source in the camera in [mm] from the given pointing Azimuth and Zenith
     * Code by F.Temme
     * @param pointingAz
     * @param pointingZe
     * @param sourceAz
     * @param sourceZe
     * @return
     */
    public double[] getSourcePosition(double pointingAz, double pointingZe, double sourceAz, double sourceZe)
    {

        double az     = pointingAz / 180 * Math.PI;
        double zd     = pointingZe / 180 * Math.PI;

        double x            = Math.sin(sourceZe / 180 * Math.PI) *Math.cos(sourceAz / 180 * Math.PI);
        double y            = Math.sin(sourceZe / 180 * Math.PI) * Math.sin(sourceAz / 180 * Math.PI);
        double z            = Math.cos(sourceZe / 180 * Math.PI);

        double x_rot        = 0;
        double y_rot        = 0;
        double z_rot        = 0;

        x_rot   = -Math.sin(-zd)*z - Math.cos(-zd)*( Math.cos(-az)*x - Math.sin(-az)*y );
        y_rot   = Math.sin(-az)*x + Math.cos(-az)*y;
        z_rot   = Math.cos(-zd)*z - Math.sin(-zd)*( Math.cos(-az)*x - Math.sin(-az)*y );
        double[] r ={ x_rot * (-distanceToEarthCenter) / z_rot ,y_rot * (-distanceToEarthCenter) / z_rot };

        return r;
    }


    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public void setX(Double x) {
        this.x = x;
    }
    public void setY(Double y) {
        this.y = y;
    }

    public String getSourceZdKey() {
		return sourceZdKey;
	}

	public void setSourceZdKey(String sourceZdKey) {
		this.sourceZdKey = sourceZdKey;
	}

	public String getSourceAzKey() {
		return sourceAzKey;
	}

	public void setSourceAzKey(String sourceAzKey) {
		this.sourceAzKey = sourceAzKey;
	}

	public String getPointingZdKey() {
		return pointingZdKey;
	}

	public void setPointingZdKey(String pointingZdKey) {
		this.pointingZdKey = pointingZdKey;
	}

	public String getPointingAzKey() {
		return pointingAzKey;
	}

	public void setPointingAzKey(String pointingAzKey) {
		this.pointingAzKey = pointingAzKey;
	}

	public void setAuxService(AuxFileService auxService) {
        this.auxService = auxService;
    }

    public void setTrackingFileUrl(SourceURL trackingFileUrl) { this.trackingFileUrl = trackingFileUrl; }

    public void setSourceFileUrl(SourceURL sourceFileUrl) {
        this.sourceFileUrl = sourceFileUrl;
    }

}
