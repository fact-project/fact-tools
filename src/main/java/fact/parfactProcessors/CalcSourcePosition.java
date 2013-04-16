package fact.parfactProcessors;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Processor;
import stream.Data;
import uk.ac.starlink.util.DataSource;
import uk.ac.starlink.util.FileDataSource;
import uk.ac.starlink.util.URLDataSource;
import fact.Constants;
import fact.image.overlays.SourceOverlay;
import fact.io.FitsDataStream;

/**
 * This is supposed to calculate the position of the source in the camera. 
 * Original C++ Code by F.Temme
 *  @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt; , Fabian Temme &lt;fabian.temme@tu-dortmund.de&gt;
 */
public class CalcSourcePosition implements Processor {


	static Logger log = LoggerFactory.getLogger(CalcSourcePosition.class);

	String drsFile = null;

	Data drsData = null;

	boolean keepData = true;


	double                          mPointingRightAscension;
    double                          mPointingDeclination;

    double                          mPointingAzimuth;
    double                          mPointingZenith;

    
    double	sourceRightAscension = 0;
    public double getSourceRightAscension() {	return sourceRightAscension; }
	public void setSourceRightAscension(double sourceRightAscension) {	this.sourceRightAscension = sourceRightAscension; }

	
	double	sourceDeclination = 0;    
	public double getSourceDeclination() {	return sourceDeclination;	}
	public void setSourceDeclination(double sourceDeclination) {	this.sourceDeclination = sourceDeclination;	}

	
	double                          mSourceAzimuth;
    double                          mSourceZenith;

    double                          mPointingNominalZenith;
    double                          mPointingNominalAzimuth;

    double                          mSourceX;
    double                          mSourceY;
/**
 * Hardcoded values of the telescopes position 
 * 
 */
    double mLongitude                  = -17.890701389;
    double mLatitude                   = 28.761795;
    double mDistance                   = 4890.0;
    double eventTime;

	/**
	 * @return the drsFile
	 */
	public String getDrsFile() {
		return drsFile;
	}

	/**
	 * @param drsFile
	 *            the drsFile to set
	 */
	public void setDrsFile(String drsFile) {
		File file = new File(drsFile);
		if (!file.canRead()) {
			throw new RuntimeException("Cannot open file " + drsFile
					+ " for reading!");
		}

		try {
			loadDrsData(new FileDataSource(file));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}

		this.drsFile = drsFile;
	}

	public void setDrsUrl(String urlString) {
		try {
			URL url = new URL(urlString);
			loadDrsData(new URLDataSource(url));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * This is just an alias for the drsFile parameter.
	 * 
	 * @return
	 */
	public String getFile() {
		return getDrsFile();
	}

	/**
	 * This is just an alias for the drsFile parameter.
	 */
	public void setFile(String file) {
		this.setDrsFile(file);
	}

	/**
	 * @return the keepData
	 */
	public boolean isKeepData() {
		return keepData;
	}

	/**
	 * @param keepData
	 *            the keepData to set
	 */
	public void setKeepData(boolean keepData) {
		this.keepData = keepData;
	}
	
	FitsDataStream stream;

	/**

	 * @param in
	 */
	protected void loadDrsData(DataSource in) {
		try {

			stream = new FitsDataStream(in);
			stream.init();
			drsData = stream.readNext();
			eventTime =	Double.parseDouble(drsData.get("Time").toString()) + 40587;
			
			mPointingRightAscension = Double.parseDouble( drsData.get("Ra").toString());
			mPointingRightAscension = mPointingRightAscension/24 *360.0;
			mPointingDeclination = Double.parseDouble( drsData.get("Dec").toString());
			
			mPointingNominalAzimuth = Double.parseDouble( drsData.get("Az").toString());
			mPointingNominalZenith= Double.parseDouble( drsData.get("Zd").toString());
			
			
			
		} catch (Exception e) {

			log.error("Failed to load DRS data: {}", e.getMessage());
			if (log.isDebugEnabled())
				e.printStackTrace();

			this.drsData = null;


			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * @see fact.data.FactProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data data) {
		//TODO: make sure to update eventime correctly. ask fabian for conversion
//		try {
////			drsData = stream.readNext() ;
//			if(drsData != null ) {
//				eventTime =	Double.parseDouble(drsData.get("Time").toString()) + 40587;
//			}
////		Date date = new Date(utc[0] * 1000L);
//			System.out.println("Eventimte in data: " + (mjdToGmst(utc[0]))  + " Eventime in drsData: " + eventTime);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("evenTime:" + eventTime);
		if(sourceRightAscension == 0 || sourceDeclination == 0 ) {
			log.info("sourceRightAscension or sourceDeclination isnt set. Using the crab nebula as source");
			sourceIsCrab();
		}
//		sourceIsMrk421();
		calculateSrcPosInCamera();
		
		//add circle overlay to map
		data.put(Constants.KEY_SOURCE_POSITION_OVERLAY, new SourceOverlay(mSourceX, mSourceY) );
		data.put(Constants.SOURCE_POS_X, mSourceX);
		data.put(Constants.SOURCE_POS_Y, mSourceY);
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
	void rotateToZdAz(double ra, double dec, boolean b)
	{

	    double phi              = ra / 180.0 * Math.PI;
	    double theta            = (90 - dec) / 180.0 * Math.PI;
	    double gmst        =  mjdToGmst(eventTime);

	    double x                =Math.sin(theta) *Math.cos(phi);
	    double y                =Math.sin(theta) *Math.sin(phi);
	    double z                =Math.cos(theta);

	    double phi_rot_angle    = gmst + (mLongitude / 180.0 * Math.PI);
	    double theta_rot_angle  = (mLatitude - 90) / 180.0 * Math.PI;

//	    double m_xx             = -cos(theta_rot_angle) *Math.cos(phi_rot_angle);
//	    double m_xy             = -cos(theta_rot_angle) *Math.sin(phi_rot_angle);
//	    double m_xz             = -sin(theta_rot_angle);

	    double m_yx             = -Math.sin(phi_rot_angle);
	    double m_yy             =  Math.cos(phi_rot_angle);

	    double m_zx             = -Math.sin(theta_rot_angle) *Math.cos(phi_rot_angle);
	    double m_zy             = -Math.sin(theta_rot_angle) *Math.sin(phi_rot_angle);
	    double m_zz             =  Math.cos(theta_rot_angle);


//	    double x_rot            = m_xx * x + m_xy * y + m_xz * z;
	    double y_rot            = m_yx * x + m_yy * y;
	    double z_rot            = m_zx * x + m_zy * y + m_zz * z;

	    double theta_rot        = Math.acos(z_rot);
	    double phi_rot          = Math.asin( y_rot /Math.sin(theta_rot) );

	    if (b){ mPointingAzimuth      = phi_rot / Math.PI * 180.0;
	    		mPointingZenith		= theta_rot / Math.PI * 180.0;
	    } else {
	    		mSourceAzimuth 		= phi_rot / Math.PI * 180.0;
	    		mSourceZenith 		= theta_rot / Math.PI * 180.0;
	    }
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

	void rotatePointingTo00()
	{

	    double az     = mPointingAzimuth / 180 * Math.PI;
	    double zd     = mPointingZenith / 180 * Math.PI;

	    double x            = Math.sin(mSourceZenith / 180 * Math.PI) *Math.cos(mSourceAzimuth / 180 * Math.PI);
	    double y            = Math.sin(mSourceZenith / 180 * Math.PI) * Math.sin(mSourceAzimuth / 180 * Math.PI);
	    double z            = Math.cos(mSourceZenith / 180 * Math.PI);

	    double x_rot        = 0;
	    double y_rot        = 0;
	    double z_rot        = 0;

	    x_rot   =  Math.sin(-az)*x + Math.cos(-az)*y;
	    y_rot   = -Math.sin(-zd)*z - Math.cos(-zd)*( Math.cos(-az)*x - Math.sin(-az)*y );
	    z_rot   =  Math.cos(-zd)*z - Math.sin(-zd)*( Math.cos(-az)*x - Math.sin(-az)*y );

	    mSourceX            = x_rot * (-mDistance) / z_rot;
	    mSourceY            = y_rot * (-mDistance) / z_rot;
	}
	
	void calculateSrcPosInCamera()
	{
//	    RotateToZdAz(mPointingRightAscension,mPointingDeclination,&mPointingAzimuth,&mPointingZenith);
//	    RotateToZdAz(mSourceRightAscension,mSourceDeclination,&mSourceAzimuth,&mSourceZenith);
//	    RotatePointingTo00();
	    rotateToZdAz(mPointingRightAscension,mPointingDeclination, true); // mPointingAzimuth, mPointingZenith);
	    rotateToZdAz(sourceRightAscension,sourceDeclination, false);
	    rotatePointingTo00();
	}

}