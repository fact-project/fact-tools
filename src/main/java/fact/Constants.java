package fact;

public class Constants {
	//key strings for the map
	public static final String DEFAULT_KEY_CALIBRATED = "DataCalibrated";

	public static final String KEY_EVENT_NUM = "EventNum";
	public static final String KEY_TRIGGER_TYPE = "TriggerType";
	public static final String PIXELSET = "pixelset";

	public static final String KEY_SOURCE_POSITION_OVERLAY= "sourcePositionOverlay" ;
	public static final String KEY_COLOR = "DrawColor";
	public static final String ELLIPSE_OVERLAY = "ellipse_overlay";
	
	

	//number of pixels
	public static final int NUMBEROFPIXEL= 1440;
	
	//filter coefficents
	public static final double[] COEFFICENTS_CFD = {-0.75,0,0,0,0,0,0,0,0,1};
	public static final double[] COEFFICENTS_N5 = {0.2,0.2,0.2,0.2,0.2};
//	public static final double[] COEFFICENTS_LAPLACE = {1, -2, 1}; 
	public static final double[] COEFFICENTS_REMOVE_SIGNAL = {-0.5, 1, -0.5};
	public static final double[] COEFFICENTS_N3 = {1.0/3.0, 1.0/3.0, 1.0/3.0};
	
	
	public static final double PIXEL_SIZE = 9.5; //9.5mm

}
