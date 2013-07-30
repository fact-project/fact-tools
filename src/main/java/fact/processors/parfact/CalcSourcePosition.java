package fact.processors.parfact;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.io.SourceURL;
import fact.Constants;
import fact.image.overlays.SourceOverlay;
import fact.io.FitsStream;

/**
 *  This is supposed to calculate the position of the source in the camera. 
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt; , Fabian Temme &lt;fabian.temme@tu-dortmund.de&gt;
 */
public class CalcSourcePosition implements StatefulProcessor {
	static Logger log = LoggerFactory.getLogger(CalcSourcePosition.class);

	Data slowData = null;
	private String outputKey = "sourcePosition";
	private String physicalSource = "crab";

	double	sourceRightAscension = 0;
	double	sourceDeclination = 0;    

	//position of the Telescope
	static final double mLongitude                  = -17.890701389;
	static final double mLatitude                   = 28.761795;
	//Distance from earth center
	static final double mDistance                   = 4890.0;
	//This is a counter to access the right rows in the tracking_file
	int timeIndex = 0;

	//The url to the TRACKING_POSITION slow control file
	private SourceURL trackingUrl;
	//This list will be populated with 
	private ArrayList<double[]> locList = new ArrayList<double[]>();

	@Override
	public void finish() throws Exception {
		// TODO Auto-generated method stub
	}
	/**
	 * read the complete TRACKING_POSITION file and save the values in the locList.
	 * For the calculation of the appropriate sky coordinates (that is Azimuth and Zenith) we only need the values "Time", "Ra" and "Dec". 
	 * There are also values for "Az" and "Zd" in file. These are calculated by the drive system itself. They can be used for a sanity check. 
	 * These values differ by what seems to be a constant amount in both Az and Zd. This is an expected deviation of 1 to 3 degrees.
	 * The time unit in the TRACKING file is in unixtime/86400.0. Its still called MJD for some reason.
	 * 
	 * The correct conversion would be: 
	 * mjd  =  timestamp/86400.0 +  2440587.5d
	 * for some effing reason. To get the correct coordinates we have to do it like this:
	 * mjd  =  timestamp/86400.0 +  2440587.0d
	 */
	@Override
	public void init(ProcessContext arg0) throws Exception {
		FitsStream stream = new FitsStream(trackingUrl);
		try {

			stream.init();
			slowData = stream.readNext();
			while(slowData !=  null){
				// Eventime, Ra, Dec, Az, Zd
				double[] pointRaDec = new double[5];
				pointRaDec[0] =	Double.parseDouble(slowData.get("Time").toString()) + 2440587.0d; //usually + 0.5

				double ra = Double.parseDouble( slowData.get("Ra").toString());
				pointRaDec[1] = ra/24 *360.0;
				pointRaDec[2] = Double.parseDouble( slowData.get("Dec").toString());

				pointRaDec[3] = Double.parseDouble( slowData.get("Az").toString());
				pointRaDec[4]= Double.parseDouble( slowData.get("Zd").toString());

				locList.add(pointRaDec);
				slowData = stream.readNext();
			}

			stream.close();
		}catch (NumberFormatException e){
			log.error("Could not parse the values from the TRACKING_POSITION file: {}", e.getMessage());
			stream.close();
		} catch (Exception e) {
			log.error("Failed to load data from TRACKING_POSITION file: {}", e.getMessage());
			e.printStackTrace();
			this.slowData = null;
			stream.close();
			throw new RuntimeException(e.getMessage());
		}

		//check the physicalSource parameter from the user
		if(sourceRightAscension == 0 || sourceDeclination == 0 ) {
			if(physicalSource.toLowerCase().equals("crab")){
				sourceIsCrab();
				log.info("Using the crab nebula as source");
			} else if (physicalSource.toLowerCase().equals("mrk421")){
				sourceIsMrk421();
				log.info("Using the mrk421 as source");
			} else {
				sourceIsCrab();
				log.warn("physicalsource or sourceRightAscension and sourceDeclination isnt set. Using the crab nebula as source");
			}
		}
	}
	@Override
	public void resetState() throws Exception {
		// TODO Auto-generated method stub
	}

	/**
	 * Here we read the eventtime from the current dataitem and convert it to 
	 * 1. unixtime 
	 * 2. mjd
	 * 3. gmst
	 * The conversion steps are necessary because I stole the mjd2gmst conversion from Fabian Temme and dont know how to get gmst dirtectly from unixtime.
	 * The unixtimestamp in the data file is saved as an array with two elements. {seconds, miroseconds} it is unclear what to do with the second one. I simply used the sum of both in seconds.. 
	 * Eventhough the numbers are small enough to NOT make a difference anyways.
	 * After reading the EventTime from the data we check which datapoint from the slowcontroll file we have to use by comparing the times. We use the point closest in time to the current dataitem.
	 * 
	 * @see fact.data.FactProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data data) {

		int[] eventTime = (int[]) data.get("UnixTimeUTC");
		long  timestamp = ((long)eventTime[0])  + ( ((long)eventTime[1])/1000000) ; 
		double mjd  =  ((double) timestamp)/86400.0 +  40587.0d+2400000.0; // usually  + 0.5 here
		double gmst =  mjdToGmst(mjd);
		double[] point = null;
		double t = 0;
		double t1 = 0;
		//check which point to use

		if(timeIndex < locList.size()-1){
			t = (locList.get(timeIndex))[0];
			t1 = (locList.get(timeIndex+1))[0];
			while(!( t < mjd && mjd < t1) && timeIndex < locList.size()-2){
					timeIndex++;
					t = (locList.get(timeIndex))[0];
					t1 = (locList.get(timeIndex+1))[0];
			}
			if(Math.abs(mjd-t) < Math.abs(mjd -t1) ){
				point = locList.get(timeIndex);
			} else {
				point = locList.get(timeIndex+1);
			}
		} else {
			log.warn("End of TRACKING file reached. Source position might be wrong");
			point = locList.get(timeIndex);
		}

		if (point == null){
			log.error("Did not get the right point from the list. point was null");
		}

		double[] pointingAzDe = getAzZd(point[1], point[2], gmst);
//		System.out.println("Az: "+  pointingAzDe[0] + " Zd: " + pointingAzDe[1] );
//		System.out.println("Az: "+  point[3] + " Zd: " + point[4] );
		double[] sourceAzDe = getAzZd(sourceRightAscension, sourceDeclination, gmst);
		double[] sourcePosition =  getSourcePosition(pointingAzDe[0], pointingAzDe[1], sourceAzDe[0], sourceAzDe[1]);

		//add circle overlay to map
		data.put(Constants.KEY_SOURCE_POSITION_OVERLAY, new SourceOverlay((float) sourcePosition[0], (float) sourcePosition[1]) );
		//add source position to dataitem
		float[] source = {(float) sourcePosition[0], (float) sourcePosition[1]};
//		System.out.println("x: "+  source[0] + " y: " +source[1] );
		data.put(outputKey, source);
		//add deviation between the calculated point az,dz and the az,dz in the file
		float[] deviation = {(float) (pointingAzDe[0] - point[3]), (float) ( pointingAzDe[1] - point[4]) };
		data.put(outputKey+"pointingDeviation", deviation);
		return data;
	}

	void sourceIsCrab()
	{
		sourceRightAscension       = (5.0 + 34.0/60 + 31.97/3600) / 24.0 * 360.0;
		sourceDeclination          = 22.0 + 0.0/60 + 52.10/3600;
	}

	void sourceIsMrk421()
	{
		sourceRightAscension       = (11.0 + 4.0/60 + 27.0/3600) / 24.0 * 360.0;
		sourceDeclination          = 38.0 + 12.0/60 + 32.0/3600;

	}

	void setRaDec(double ra, double dec)
	{
		sourceRightAscension       = ra;
		sourceDeclination          = dec;
	}
	/**
	 * This is an adaption of the C++ Code by F.Temme
	 * @param ra
	 * @param dec
	 * @param gmst the Eventtime of the current event in gmst format
	 * @return an array of length 2 containing {azimuth, zenith};
	 */
	double[] getAzZd(double ra,double dec, double gmst){
		double phi              =  ra / 180.0 * Math.PI;
		double theta            =  (90 - dec) / 180.0 * Math.PI;

		double x                =Math.sin(theta) *Math.cos(phi);
		double y                =Math.sin(theta) *Math.sin(phi);
		double z                =Math.cos(theta);

		double phi_rot_angle    = gmst + (mLongitude / 180.0 * Math.PI);
		double theta_rot_angle  = (mLatitude - 90) / 180.0 * Math.PI;

		//		    double m_xx             = -cos(theta_rot_angle) *Math.cos(phi_rot_angle);
		//		    double m_xy             = -cos(theta_rot_angle) *Math.sin(phi_rot_angle);
		//		    double m_xz             = -sin(theta_rot_angle);

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

	//Code by fabian temme
	private double mjdToGmst(double mjd) {
		// nach Jean Meeus: Astronomical Algorithms, 2. Auflage, Willman-Bell,
		// Rochmond Virginia 1998, ISBN 0-943396-61-1 (Literatur von Wiki entnommen)
		/// @todo in literatur nachschlagen
		double mjd_centurie_J2000      = (mjd - 51544.5) / 36525.0;
		double[] constants =  {280.46061837, 13185000.77, 2577.765, 38710000};

		// gmst in multiple of 360
		double gmst    = constants[0]
				+ ( constants[1] * mjd_centurie_J2000 )
				+ ( mjd_centurie_J2000 * mjd_centurie_J2000 / constants[2] )
				- ( mjd_centurie_J2000 * mjd_centurie_J2000
						* mjd_centurie_J2000 / constants[3] );

		gmst = gmst % 360.0;

		gmst = gmst / 180.0 * Math.PI;

		return gmst;
	}

	private double[] getSourcePosition(double pointingAz, double pointingZe, double sourceAz, double sourceZe)
	{

		double az     = pointingAz / 180 * Math.PI;
		double zd     = pointingZe / 180 * Math.PI;

		double x            = Math.sin(sourceZe / 180 * Math.PI) *Math.cos(sourceAz / 180 * Math.PI);
		double y            = Math.sin(sourceZe / 180 * Math.PI) * Math.sin(sourceAz / 180 * Math.PI);
		double z            = Math.cos(sourceZe / 180 * Math.PI);

		double x_rot        = 0;
		double y_rot        = 0;
		double z_rot        = 0;

		x_rot   =  Math.sin(-az)*x + Math.cos(-az)*y;
		y_rot   = -Math.sin(-zd)*z - Math.cos(-zd)*( Math.cos(-az)*x - Math.sin(-az)*y );
		z_rot   =  Math.cos(-zd)*z - Math.sin(-zd)*( Math.cos(-az)*x - Math.sin(-az)*y );
		double[] r ={ x_rot * (-mDistance) / z_rot ,y_rot * (-mDistance) / z_rot };
		return r;
	}




	public String getOutputKey() {
		return outputKey;
	}
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}


	@Parameter(description = "A URL to the FITS file.")
	public void setUrl(URL url) {
		trackingUrl = new SourceURL(url);
	}

	@Parameter(description = "A String with a valid URL FITS file.")
	public void setUrl(String urlString) {
		try{
			URL url = new URL(urlString);
			trackingUrl = new SourceURL(url);
		} catch (MalformedURLException e) {
			log.error("Malformed URL. The URL parameter of this processor has to a be a valid url");
			throw new RuntimeException("Cant open drsFile");
		}
	}


	public double getSourceDeclination() {	
		return sourceDeclination;	
	}
	public void setSourceDeclination(double sourceDeclination) {
		this.sourceDeclination = sourceDeclination;	
	}

	public double getSourceRightAscension() {	
		return sourceRightAscension; 
	}
	public void setSourceRightAscension(double sourceRightAscension) {	
		this.sourceRightAscension = sourceRightAscension; 
	}
	public String getPhysicalSource() {
		return physicalSource;
	}
	public void setPhysicalSource(String physicalSource) {
		this.physicalSource = physicalSource;
	}

}