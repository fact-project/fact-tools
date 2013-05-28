package fact;

public class Constants {
	//key strings for the map
	public static final String DEFAULT_KEY_MC = "DataMC";
	public static final String DEFAULT_KEY_CALIBRATED = "DataCalibrated";
	public static final String DEFAULT_KEY_MC_CALIBRATED = "DataMCCalibrated";

	public static final String DEFAULT_KEY = "Data";
	public static final String KEY_EXFIT = "ExFit";
	public static final String KEY_EVENT_NUM = "EventNum";
	public static final String KEY_TRIGGER_TYPE = "TriggerType";
	public static final String KEY_MAX_AMPLITUDE_POSITIONS = "maxAmplitudePositions"; 
	public static final String KEY_MAX_AMPLITUDES = "amplitudes"; 
//	public static final String KEY_ARRIVALTIMES= "arrivaltimes";
	public static final String KEY_PHOTONCHARGE= "photoncharge";
	public static final String KEY_SIMPLE_CLEAN_COREPIXEL= "simpleCorePixel";
	public static final String KEY_CORENEIGHBOURCLEAN= "coreNeighbourPixel";
	public static final String KEY_STD = "standardDev";
	public static final String KEY_FIR_RESULT = "firResult";
	public static final String RISINGEDGEPOSITION = "risingEdgePosition";
	public static final String KEY_AVERAGES = "averages";
	public static final String KEY_SPIKES_REMOVED = "spikedRemovedData";
	public static final String KEY_STD_SHOWER = "standardDevShower";
	public static final String KEY_EXPONENTIALY_SMOOTHED = "exponentiallySmoothedData";
	public static final String KEY_TIME_MEDIAN_CLEAN = "timeMedianClean";
	public static final String PIXELSET = "pixelset";
	public static final String REMOVE_SPIKES_MARS = "spikeRemovedMars";
	public static final String KEY_PLOT_COLORMAP = "@plotPanelColor";
	public static final String KEY_EVENT_TIME = "UnixTimeUTC";
	public static final String KEY_NORMALIZED_SLICES= "normalizedSlices";
	public static final String KEY_SOURCE_POSITION_OVERLAY= "sourcePositionOverlay" ;
	public static final String KEY_NUMBER_ISLANDS =  "numberOfIslands";
	public static final String KEY_SHOWER_PHOTONCHARGE = "showerPhotonCharge";
	public static final String KEY_SHOWER_ARRIVALTIME_DEV = "stdDevArrivalTimes";
	public static final String KEY_COLOR = "DrawColor";
	public static final String KEY_INTERPOLATED_DATA ="interpolatedPixel";
//    mHillasParameterWritingStream << "Run;evnr;Size;Width;Length;Area;" ;
//    mHillasParameterWritingStream << "Delta;Alpha;AlphaOff1;AlphaOff2;AlphaOff3;";
//    mHillasParameterWritingStream << "Distance;Concentration1Pixel;Concentration2Pixel;" ;
//    mHillasParameterWritingStream << "CogX;CogY;SourceRa;SourceDec;";
//    mHillasParameterWritingStream << "SourceX;SourceY;";
//    mHillasParameterWritingStream << "NominalZenith;NominalAzimuth;NumberIslands;" ;
//    mHillasParameterWritingStream << "NumberShowerPixel;LeakageBorder;LeakageSecondBorder;EventTime" << endl;
	//Hillas and Ellipse parameter names
	public static final String ELLIPSE_DELTA  = "EllipseDelta";
	public static final String ELLIPSE_ALPHA  = "EllipseAlpha";
	public static final String ELLIPSE_ALPHA_1  = "EllipseAlpha1";
	public static final String ELLIPSE_ALPHA_2  = "EllipseAlpha2";
	public static final String ELLIPSE_ALPHA_3  = "HillasAlpha3";
	public static final String ELLIPSE_LENGTH  = "EllipseLength";
	public static final String ELLIPSE_AREA  = "EllipseArea";
	public static final String ELLIPSE_WIDTH  = "EllipseWidth";
	public static final String ELLIPSE_SIZE  = "EllipseShowerSize";
	public static final String ELLIPSE_DISTANCE = "EllipseDistance";
	public static final String HILLAS_LEAKAGE_BORDER  = "HillasLeakageBorder";
	public static final String HILLAS_LEAKAGE_SECONDBORDER  = "HillasLeakageSecondBorder";
	public static final String HILLAS_CONCENTRATION1  = "HillasConcentration1";
	public static final String HILLAS_CONCENTRATION2  = "HillasConcentration2";
	public static final String ELLIPSE_OVERLAY = "ellipse_overlay";
	public static final String HILLAS_NUMBER_ISLANDS = "HillasNumberOfIslands";
	
	
	//number constants for hardware magic.
	public static final float PIXELGAIN= 9.0f;
	
	//number of threads. temporarily hardcoded
	public static final int  NUMBEROFTHREADS= 4;
	
	//number of pixels
	public static final int NUMBEROFPIXEL= 1440;
	
	//filter coefficents
	public static final double[] COEFFICENTS_CFD = {-0.75,0,0,0,0,0,0,0,0,1};
	public static final double[] COEFFICENTS_N5 = {0.2,0.2,0.2,0.2,0.2};
//	public static final double[] COEFFICENTS_LAPLACE = {1, -2, 1}; 
	public static final double[] COEFFICENTS_REMOVE_SIGNAL = {-0.5, 1, -0.5};
	public static final double[] COEFFICENTS_N3 = {1.0/3.0, 1.0/3.0, 1.0/3.0};
	
	
	public static final double PIXEL_SIZE = 9.5; //9.5mm
	public static final String SOURCE_POS_X = "sourceX";
	public static final String SOURCE_POS_Y = "sourceY";
	public static final String ERROR_WRONG_KEY = "Key not found in Event: ";
	public static final String EXPECT_ARRAY_F = "This processor operates on float arrays. ";
	public static final String PLOT_AREAVSSIZE= "areaVsSize";
	public static final String PLOT_ANGLE_HISTOGRAM ="alphavscount";
	public static final String PLOT_PER_PIXEL = "perPixelPlot";
	public static final String PLOT_FILE_SEPARATOR = "    ";
	public static final String EXPECT_ARRAY = "This processor operates on float, double or int arrays. ";
	public static final String KEY_DIFF = "Diff";
}
