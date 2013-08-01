/**
 * 
 */
package fact.processors.parfact;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.data.EventUtils;
import fact.viewer.ui.DefaultPixelMapping;

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
	static Logger log = LoggerFactory.getLogger(HillasParameter.class);
	private  float corePixelThreshold = 5.0f;
	private  float neighborPixelThreshold = 2.0f;
	private int minSize = 2;
	private double showerThreshold = 0.8;
	/**
	 * members from the original c++ code
	 */
	private float leakageBorder;
	private float leakageSecondBorder;
	private float concentration1Pixel;
	private float concentration2Pixel;
	private ShowerEllipse ellipse;
	private int numberOfIslands;
	private float showerSize;
	private boolean hasShower;
	
	private String key = "DataCalibrated";
	private String outputKey = key;
	private String pixels = "showerPixel";
	private int[] showerPixelArray =  null;
	private String photonEquivalent="photonCharge";
	private float[] pE = null;
	private String sourcePosition = "sourcePosition";
	private float[] source = null;

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
		for(int nPix: DefaultPixelMapping.getNeighborsFromSoftID(pix))
		{
			if(isBorderPixel(nPix)){
				return true;
			}
		}
		
		return false;
	}
	boolean isBorderPixel(int pix){
		for(int i : DefaultPixelMapping.getNeighborsFromSoftID(pix)){
			if(i == -1) return true;
		}
		return false;
	}
	
	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {
	
		try{
			showerPixelArray = (int[]) input.get(pixels);
			pE = (float[]) input.get(photonEquivalent);
			source  = (float[]) input.get(sourcePosition);
		} catch (ClassCastException e){
			log.error("wrong types" + e.toString());
		}
		if(showerPixelArray == null || pE ==null ||source==null){
			log.error("Map does not conatin the right values for the keys");
			return null;
		}
		
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
				if(hasShower) {
					input.put(outputKey+"Delta", ellipse.mDelta);
					input.put(outputKey+"Alpha", ellipse.alpha);
					input.put(outputKey+"AlphaOff1", ellipse.alphaOff1);
					input.put(outputKey+"AlphaOff2", ellipse.alphaOff2);
					input.put(outputKey+"AlphaOff3", ellipse.alphaOff3);
					input.put(outputKey+"Area", ellipse.area);
					input.put(outputKey+"Size", ellipse.size);
					input.put(outputKey+"Distance", ellipse.mDistance);
					input.put(outputKey+"Width", ellipse.width);
					input.put(outputKey+"Length", ellipse.length);
					input.put(outputKey+"Concentration", concentration1Pixel);
					input.put(outputKey+"Concentration2", concentration2Pixel);
					input.put(outputKey+"Leakage", leakageBorder);
					input.put(outputKey+"Secondleakage", leakageSecondBorder);
//					input.put(outputKey+"alphaOff1", numberOfIslands);
//					input.put(Constants.ELLIPSE_OVERLAY, new EllipseOverlay(ellipse.centerX, ellipse.centerY, ellipse.width, ellipse.length, ellipse.auxilaryDistance , ellipse.mDelta) );
				} 				

		return input;

	}	
	public void processEvent(Data input, String key) {
		
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
		
		if(showerPixelArray.length == 0) {
			//no shower in event detected
			hasShower = false;
			return;
		} else {
			hasShower = true;
		}
		ellipse = new ShowerEllipse(showerPixelArray, pE,  source[0], source[1]);
		
		//This will calculate the ellipse properties asymetry and centerofgravity
//		ellipse = new ShowerEllipse(showerPixelArray, photonCharges, source_x, source_y);
		
		//now some shower specific properties
		numberOfIslands =  calculateNumberOfIslands(showerPixelArray);
//		System.out.println("Hillas numIslands: " + numberOfIslands + " hillas showerpixelnumber: " + showerPixelArray.length);
	   	showerSize = calculateSize(showerPixelArray, pE);
//	   	System.out.println("showersize" + showerSize);
	    //sets the two leakage globals
	    calculateLeakage(showerPixelArray, pE, showerSize);
	    //sets concentration1Pixel and concentration2Pixel
	    calculateConcentration(showerPixelArray, pE, showerSize);


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

	public String getPixels() {
		return pixels;
	}
	public void setPixels(String pixels) {
		this.pixels = pixels;
	}

	public String getPhotonEquivalent() {
		return photonEquivalent;
	}
	public void setPhotonEquivalent(String photonEquivalent) {
		this.photonEquivalent = photonEquivalent;
	}

	public String getSourcePosition() {
		return sourcePosition;
	}
	public void setSourcePosition(String sourcePosition) {
		this.sourcePosition = sourcePosition;
	}

	public String getOutputKey() {
		return outputKey;
	}
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}
	
}