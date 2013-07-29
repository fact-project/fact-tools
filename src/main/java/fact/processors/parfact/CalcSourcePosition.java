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
 * This is supposed to calculate the position of the source in the camera. 
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt; , Fabian Temme &lt;fabian.temme@tu-dortmund.de&gt;
 */
public class CalcSourcePosition implements StatefulProcessor {


	static Logger log = LoggerFactory.getLogger(CalcSourcePosition.class);


	Data drsData = null;
	private String outputKey = "sourcePosition";

	double	sourceRightAscension = 0;
	double	sourceDeclination = 0;    
	
	private String physicalSource = "crab";



	/**
	 * Hardcoded values of the telescopes position 
	 * 
	 */
	double mLongitude                  = -17.890701389;
	double mLatitude                   = 28.761795;
	double mDistance                   = 4890.0;


	FitsStream stream;

	int timeIndex = 0;

	private SourceURL trackingUrl;
	private ArrayList<double[]> locList = new ArrayList<double[]>();

	@Override
	public void finish() throws Exception {
		// TODO Auto-generated method stub

	}
	@Override
	public void init(ProcessContext arg0) throws Exception {
		try {

			stream = new FitsStream(trackingUrl);
			stream.init();
			drsData = stream.readNext();
			while(drsData !=  null){
				// Eventime, Ra, Dec, Az, Zd
				double[] pointRaDec = new double[5];
				pointRaDec[0] =	Double.parseDouble(drsData.get("Time").toString()) + 2440587.5d;

				double ra = Double.parseDouble( drsData.get("Ra").toString());
				pointRaDec[1] = ra/24 *360.0;
				pointRaDec[2] = Double.parseDouble( drsData.get("Dec").toString());

				pointRaDec[3] = Double.parseDouble( drsData.get("Az").toString());
				pointRaDec[4]= Double.parseDouble( drsData.get("Zd").toString());
				System.out.println("Right Ascension: " + pointRaDec[1] + "  Declination:  " + pointRaDec[2] + "  Azimuth: " + pointRaDec[3] + "  Zenith: " + pointRaDec[4]);
				locList.add(pointRaDec);
				drsData = stream.readNext();
			}

			stream.close();
		} catch (Exception e) {
			log.error("Failed to load DRS data: {}", e.getMessage());
				e.printStackTrace();
			this.drsData = null;
			stream.close();
			throw new RuntimeException(e.getMessage());
		}		
	}
	@Override
	public void resetState() throws Exception {
		// TODO Auto-generated method stub
	}

	/**
	 * @see fact.data.FactProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data data) {
		if(sourceRightAscension == 0 || sourceDeclination == 0 ) {
			if(physicalSource.equals("crab")){
				sourceIsCrab();
				log.info("Using the crab nebula as source");
			} else if (physicalSource.equals("mrk421")){
				sourceIsMrk421();
				log.info("Using the mrk421 as source");
			} else {
				sourceIsCrab();
				log.info("sourceRightAscension or sourceDeclination isnt set. Using the crab nebula as source");
			}
		}

		int[] eventTime = (int[]) data.get("UnixTimeUTC");
		long  timestamp = ((long)eventTime[0])*1000; 
		double mjd  =  ((double) timestamp/(1000.0))/86400.0 +  40587.5d+2400000.0;
		double gmst =  mjdToGmst(mjd);
		double[] point = null;
		double t = 0;
		if(timeIndex < locList.size()){
			t = (locList.get(timeIndex))[0];
			double t1 = (locList.get(timeIndex + 1))[0];

			if(mjd - t < mjd - t1){
				point = locList.get(timeIndex);
				if(t < mjd ){
				timeIndex++;	
				}
			} else{
				point = locList.get(timeIndex+1);
				timeIndex++;
			}
		}
		if (point == null){
			log.error("Did not get the right point from the list. point was null");
		}
		double[] pointingAzDe = getAzZd(point[1], point[2], gmst);
		double[] sourceAzDe = getAzZd(sourceRightAscension, sourceDeclination, gmst);
		double[] sourcePosition =  getSourcePosition(pointingAzDe[0], pointingAzDe[1], sourceAzDe[0], sourceAzDe[1]);

		//add circle overlay to map
		data.put(Constants.KEY_SOURCE_POSITION_OVERLAY, new SourceOverlay((float) sourcePosition[0], (float) sourcePosition[1]) );
		float[] source = {(float) sourcePosition[0], (float) sourcePosition[1]};
		data.put(outputKey, source);
		data.put("X", sourcePosition[0]);
		data.put("Y", sourcePosition[1]);
		data.put("driveX", point[0]);
		data.put("driveY", point[1]);
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
 * @return an array of length 2 containg {azimuth, zenith};
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