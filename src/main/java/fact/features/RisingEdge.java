/**
 * 
 */
package fact.features;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import fact.Constants;

/**
 * TODO: this needs to be redone. the orignal code is a joke. talk to fabian about outofbounds errors. Also CFD is better here
 * 
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class RisingEdge implements Processor {
	
	static Logger log = LoggerFactory.getLogger(RisingEdge.class);
	public RisingEdge() {
		
	}

	public RisingEdge(String[] keys) {
		this.keys=keys;
	}

	/**
     * parameter and getter setters
     */
    String[] keys = new String[] { Constants.DEFAULT_KEY };

	
	/**
	 * @return the keys
	 */
	public String[] getKeys() {
		return keys;
	}

	/**
	 * @param keys
	 *            the keys to set
	 */
	public void setKeys(String[] keys) {
		this.keys = keys;
	}



	private int searchWindowRight = 10;
	private int searchWindowLeft = 25;
//
	public int getSearchWindowRight() {
		return searchWindowRight;
	}

	public void setSearchWindowRight(int searchWindowRight) {
		this.searchWindowRight = searchWindowRight;
	}

	public int getSearchWindowLeft() {
		return searchWindowLeft;
	}

	public void setSearchWindowLeft(int searchWindowLeft) {
		this.searchWindowLeft = searchWindowLeft;
	}
	

	//In diesem array speichern wir die maximalen Amplituden fuer jedes Pixel...
	static float[] amplitudes = null;
	// Die positionen der Amplituden
	static int[] positions =  null;
	
	private boolean overwrite = true;
	
	public boolean isOverwrite() {
		return overwrite;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}
	
	/**
	 * @see stream.DataProcessor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {

		//
		//
		//String[] keys = new String[] { "Data", "DataCalibrated" };
		
		for (String key : keys) {
			if(overwrite){
				input.put(Constants.RISINGEDGEPOSITION, processEvent(input, key));
			} else {
				input.put(Constants.RISINGEDGEPOSITION + "_" + key, processEvent(input, key));
			}
		}
		return input;
	}

	public int[] processEvent(Data input, String key) {
		
		Serializable value = null;
		
		if(input.containsKey(key)){
			 value = input.get(key);
		} else {
			//key doesnt exist in map
			log.info(Constants.ERROR_WRONG_KEY + key + ",  " + this.getClass().getSimpleName() );
			return null;
		}
		
		if (value != null && value.getClass().isArray()
				&& value.getClass().getComponentType().equals(float.class)) {
			return processSeries((float[]) value);
			
		}
		//in case value in Map is of the wrong type to do this calculation
		else
		{
			return null;
		}
		
	}

	public int[] processSeries(float[] input) {
		int[] positions = new  MaxAmplitudePosition().processSeries(input);
	
		int roi = input.length / Constants.NUMBEROFPIXEL;
		
		int[] risingEdgePositions = new int[Constants.NUMBEROFPIXEL];
		/**
		 * original c++ code by F.Temme with some modifications
		 * one-liner comments by F.Temme  
		 */
		//iterate over all pixels. 			
		for (int pix = 0;  pix < Constants.NUMBEROFPIXEL; pix++ ){

		    /// temp. Variables
		    float           current_slope   = 0;
		    float           max_slope       = 0;
		    int             pos             = 0;
		    /// @todo remove magic numbers for the search window
		    int             search_window_left  = positions[pix] - 25;
		    if (search_window_left < 10)
		    {
		        search_window_left = 10;
		    }
		    int             search_window_right = positions[pix];

		    /// Loop over all timeslices of given Window
		    for( int sl = search_window_left; sl < search_window_right; sl++)
		    {
		    	if( sl + 2 < roi) {
		    		current_slope              = input[sl+2+(pix*roi)] - input[sl-2+(pix*roi)];
		    	} else {
		    		break;
		    	}
		        if (current_slope > max_slope)
		        {
		            max_slope       = current_slope;
		            pos             = sl;
		        }
		    }
		    risingEdgePositions[pix]             = pos;
			    
		}
		return risingEdgePositions;
	}


}
