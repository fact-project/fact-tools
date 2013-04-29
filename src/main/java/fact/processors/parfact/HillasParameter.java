/**
 * 
 */
package fact.processors.parfact;

import java.io.Serializable;

import org.jfree.util.Log;

import stream.Processor;
import stream.annotations.Parameter;
import stream.Data;
import fact.Constants;
import fact.data.EventUtils;
import fact.data.FactEvent;
import fact.image.overlays.EllipseOverlay;

/**
 *Calculates the Hillas Parameter from the Ellipse. Some processor to identify showerpixels has to be run before.
 * 
 * void
HillasParameter::CalculateParameter()
{
    if (mVerbosityLevel > 9)
    {
        cout << "HillasParameter::CalculateParameter() called" << endl;
    }
    SetAllParameterToZero();
    CalculateNumberOfIslands();
    CalculateSize();
    CalculateLeakage();
    CalculateConcentration();
    CalculateCenterOfGravity();
    CalculateEllipse();
    CalculateSourceParameter();
    CalculateAsymmetry();
}
 * 
 * @author Kai
 * 
 */
public class HillasParameter implements Processor {
	private String cleaningMethod = "default";
	String[] showerPixel = new String []{Constants.KEY_CORENEIGHBOURCLEAN};
	private  float corePixelThreshold = 5.0f;
	private  float neighborPixelThreshold = 2.0f;
	private int minSize = 2;
	private double showerThreshold = 0.8;
	private String key;
	/**
	 * members from the original c++ code
	 */
	private float leakageBorder;
	private float leakageSecondBorder;
	private float concentration1Pixel;
	private float concentration2Pixel;
	private ShowerEllipse ellipse;
	private Data item;
	private int numberOfIslands;
	private float showerSize;
	private boolean hasShower;
	private boolean overwrite =  true;
	
	public HillasParameter() {
		
	}
	public HillasParameter(String key) {
		this.key=key;
	}



//	public boolean isOverwrite() {
//		return overwrite;
//	}
//	public void setOverwrite(boolean overwrite) {
//		this.overwrite = overwrite;
//	}
	/**
	 * calculates the sum of all photoncharges for all showerPixel
	 */
	public float calculateSize(int[] showerPixelArray, float[] photonCharges){
		float ret = 0.0f;
		for(int pix: showerPixelArray)	ret += photonCharges[pix];
		return ret;
	}

	/**
	 * calculate leakage
	 */
	

	private void calculateLeakage(int[] showerPixel, float[] photonCharge, float size)
	{
	    leakageBorder          = 0;
	    leakageSecondBorder    = 0;
	    for (int pix: showerPixel)
	    {
	        if (isBorderPixel(pix) )
	        {
	            leakageBorder          += photonCharge[pix];
	            leakageSecondBorder    += photonCharge[pix];
	        }
	        else if (isSecondBorderPixel(pix))
	        {
	            leakageSecondBorder    += photonCharge[pix];
	        }
	    }
	    leakageBorder          = leakageBorder        / size;
	    leakageSecondBorder    = leakageSecondBorder  / size;
	}

	//this is of course not the most efficient solution
	boolean isSecondBorderPixel(int pix){
		for(int nPix: FactEvent.PIXEL_MAPPING.getNeighborsFromSoftID(pix))
		{
			if(isBorderPixel(nPix)){
				return true;
			}
		}
		
		return false;
	}
	boolean isBorderPixel(int pix){
		for(int i : FactEvent.PIXEL_MAPPING.getNeighborsFromSoftID(pix)){
			if(i == -1) return true;
		}
		return false;
	}
	
	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {
	
		
//				input.put(Constants.KEY_CORENEIGHBOURCLEAN + "_" + key, processEvent(input, key));
//				input.put(Constants.KEY_CORENEIGHBOURCLEAN+"_"+key+"_"+Constants.PIXELSET, corePixelSet);
				processEvent(input, key);
//			    mHillasParameterWritingStream << "Run;evnr;Size;Width;Length;Area;" ;
//			    mHillasParameterWritingStream << "Delta;Alpha;AlphaOff1;AlphaOff2;AlphaOff3;";
//			    mHillasParameterWritingStream << "Distance;Concentration1Pixel;Concentration2Pixel;" ;
//			    mHillasParameterWritingStream << "CogX;CogY;SourceRa;SourceDec;";
//			    mHillasParameterWritingStream << "SourceX;SourceY;";
//			    mHillasParameterWritingStream << "NominalZenith;NominalAzimuth;NumberIslands;" ;
//			    mHillasParameterWritingStream << "NumberShowerPixel;LeakageBorder;LeakageSecondBorder;EventTime" << endl;
				if(hasShower && !overwrite) {
					input.put(Constants.ELLIPSE_DELTA+"_"+key, ellipse.mDelta);
					input.put(Constants.ELLIPSE_ALPHA +"_"+key, ellipse.alpha);
					input.put(Constants.ELLIPSE_ALPHA_1 +"_"+key, ellipse.alphaOff1);
					input.put(Constants.ELLIPSE_ALPHA_2 +"_"+key, ellipse.alphaOff2);
					input.put(Constants.ELLIPSE_ALPHA_3 +"_"+key, ellipse.alphaOff3);
					input.put(Constants.ELLIPSE_AREA +"_"+key, ellipse.area);
					input.put(Constants.ELLIPSE_SIZE +"_"+key, ellipse.size);
					input.put(Constants.ELLIPSE_DISTANCE +"_"+key, ellipse.mDistance);
					input.put(Constants.ELLIPSE_WIDTH +"_"+key, ellipse.width);
					input.put(Constants.ELLIPSE_LENGTH +"_"+key, ellipse.length);
					input.put(Constants.HILLAS_CONCENTRATION1 +"_"+key, concentration1Pixel);
					input.put(Constants.HILLAS_CONCENTRATION2 +"_"+key, concentration2Pixel);
					input.put(Constants.HILLAS_LEAKAGE_BORDER+"_"+key, leakageBorder);
					input.put(Constants.HILLAS_LEAKAGE_SECONDBORDER+"_"+key, leakageSecondBorder);
					input.put(Constants.HILLAS_NUMBER_ISLANDS+"_"+key, numberOfIslands);
					input.put(Constants.ELLIPSE_OVERLAY+"_"+key, new EllipseOverlay(ellipse.centerX, ellipse.centerY, ellipse.width, ellipse.length, ellipse.auxilaryDistance , ellipse.mDelta) );
				} else if(hasShower && overwrite){
					input.put(Constants.ELLIPSE_DELTA , ellipse.mDelta);
					input.put(Constants.ELLIPSE_ALPHA  , ellipse.alpha);
					input.put(Constants.ELLIPSE_ALPHA_1  , ellipse.alphaOff1);
					input.put(Constants.ELLIPSE_ALPHA_2  , ellipse.alphaOff2);
					input.put(Constants.ELLIPSE_ALPHA_3  , ellipse.alphaOff3);
					input.put(Constants.ELLIPSE_AREA  , ellipse.area);
					input.put(Constants.ELLIPSE_SIZE  , ellipse.size);
					input.put(Constants.ELLIPSE_DISTANCE  , ellipse.mDistance);
					input.put(Constants.HILLAS_CONCENTRATION1  , concentration1Pixel);
					input.put(Constants.HILLAS_CONCENTRATION2  , concentration2Pixel);
					input.put(Constants.HILLAS_LEAKAGE_BORDER , leakageBorder);
					input.put(Constants.HILLAS_LEAKAGE_SECONDBORDER , leakageSecondBorder);
					input.put(Constants.HILLAS_NUMBER_ISLANDS , numberOfIslands);
					input.put(Constants.ELLIPSE_WIDTH, ellipse.width);
					input.put(Constants.ELLIPSE_LENGTH, ellipse.length);
					input.put(Constants.ELLIPSE_OVERLAY , new EllipseOverlay(ellipse.centerX, ellipse.centerY, ellipse.width, ellipse.length, ellipse.auxilaryDistance , ellipse.mDelta) );
				}
				
				

		return input;

		//
		//
		//String[] keys = new String[] { "Data", "DataCalibrated" };

	}
	
	public void processEvent(Data input, String key) {
		
		item = input;
		Serializable value = null;
		
		if(input.containsKey(key)){
			 value = input.get(key);
		} else {
			//key doesn't exist in map
			return;
		}
		
		if (value != null && value.getClass().isArray()
				&& value.getClass().getComponentType().equals(float.class)) {
			processSeries((float[]) value);
			
		}
		//in case value in Map is of the wrong type to do this calculation
		else
		{
			return;
		}
	
	}

	public void processSeries(float[] value) {
//	    SetAllParameterToZero();

		//TODO: allow other  cleaning methods
		//create showerPixelarray first since the other methods need it; maybe just keep this a local reference for clarity?
		CoreNeighborClean core = new CoreNeighborClean();
		core.setCorePixelThreshold(corePixelThreshold);
		core.setNeighborPixelThreshold(neighborPixelThreshold);
		int[] showerPixelArray = core.processSeries(value);
		
		
//		ArrayList<ArrayList<Integer>> ll = new StdClean().processSeries(value, showerThreshold);
//		ArrayList<Integer> l = new ArrayList<Integer>();
//		//maybe add filter for island size here
//		for(ArrayList<Integer> li :ll){
//			if(li.size() >= minSize){
//				l.addAll(li);
//			}
//		}
//		int [] showerPixelArray = new int[l.size()];
//		for(int i = 0; i< l.size();i++){
//			showerPixelArray[i] = l.get(i);
//		}
		
		if(showerPixelArray.length == 0) {
			//no shower in event detected
			hasShower = false;
			return;
		} else {
			hasShower = true;
		}
		float[] photonCharges =  new CalculatePhotonCharge().processSeries(value);
		if (item.containsKey(Constants.SOURCE_POS_X) && item.containsKey(Constants.SOURCE_POS_Y)){
			ellipse = new ShowerEllipse(showerPixelArray, photonCharges,  (Double) item.get(Constants.SOURCE_POS_X), (Double) item.get(Constants.SOURCE_POS_Y));
		} else {
			//TODO: handle tat case
//			System.out.println("call calcsourcepos before! bieatch!");
			Log.info("Source Position was not calculated. You need to call the calcSourcePosition processor before creating Hillas Parameter");
			return;

		}
		
		//This will calculate the ellipse properties asymetry and centerofgravity
//		ellipse = new ShowerEllipse(showerPixelArray, photonCharges, source_x, source_y);
		
		//now some shower specific properties
		numberOfIslands =  calculateNumberOfIslands(showerPixelArray);
//		System.out.println("Hillas numIslands: " + numberOfIslands + " hillas showerpixelnumber: " + showerPixelArray.length);
	   	showerSize = calculateSize(showerPixelArray, photonCharges);
//	   	System.out.println("showersize" + showerSize);
	    //sets the two leakage globals
	    calculateLeakage(showerPixelArray, photonCharges, showerSize);
	    //sets concentration1Pixel and concentration2Pixel
	    calculateConcentration(showerPixelArray, photonCharges, showerSize);


		return;
	}

	private int calculateNumberOfIslands(int[] showerPixel) {
//		return CoreNeighborClean.processSeries(value).size;
		
		return EventUtils.breadthFirstSearch(EventUtils.arrayToList(showerPixel)).size();
	}
	
	/**
	 * Original code by F.Temme
	 * @param showerPixel
	 * @param photonCharge
	 * @param size
	 */
	private void calculateConcentration(int[] showerPixel, float[] photonCharge, float size)
	{
	    float max_photon_charge                 = 0;
	    float second_max_photon_charge          = 0;
	    for (int pix : showerPixel)
	    {
	        if (photonCharge[pix] > max_photon_charge)
	        {
	            second_max_photon_charge        = max_photon_charge;
	            max_photon_charge               = photonCharge[pix];
	        }
	        else if (photonCharge[pix] > second_max_photon_charge)
	        {
	                second_max_photon_charge    = photonCharge[pix];
	        }
	        
	    }
	    concentration1Pixel                          = max_photon_charge / size;
	    concentration2Pixel                         = (max_photon_charge + second_max_photon_charge) / size;
	}
	
	
/*
 * Getter and Setter
 */

	public String getCleaningMethod() {
		return cleaningMethod;
	}
	public void setCleaningMethod(String cleaningMethod) {
		this.cleaningMethod = cleaningMethod;
	}


	public String[] getShowerPixel() {
		return showerPixel;
	}

	public void setShowerPixel(String[] showerPixel) {
		this.showerPixel = showerPixel;
	}
	
	
	

	public float getCorePixelThreshold() {
		return corePixelThreshold;
	}
	@Parameter(required = false, description = "The smallest PhotonCharge a Pixel must have to be identified as a CorePixel", defaultValue = "5.0")
	public void setCorePixelThreshold(float corePixelThreshold) {
		this.corePixelThreshold = corePixelThreshold;
	}



	public float getNeighborPixelThreshold() {
		return neighborPixelThreshold;
	}
	@Parameter(required = false, description = "The smallest PhotonCharge a Pixel must have thats adjacent to a previously identified corePixel", defaultValue = "2.0")
	public void setNeighborPixelThreshold(float neighborPixelThreshold) {
		this.neighborPixelThreshold = neighborPixelThreshold;
	}
	
	/**
	 * 
	 */
	
	public int getMinSize() {
		return minSize;
	}
	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}


	public double getShowerThreshold() {
		return showerThreshold;
	}
	public void setShowerThreshold(double showerThreshold) {
		this.showerThreshold = showerThreshold;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
}