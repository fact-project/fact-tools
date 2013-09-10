/**
 * 
 */
package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import fact.utils.SimpleFactEventProcessor;

/**
 * 
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class RisingEdge extends SimpleFactEventProcessor<float[], int[]> {

	static Logger log = LoggerFactory.getLogger(RisingEdge.class);

	private int searchWindowRight = 10;
	private int searchWindowLeft = 25;
	//

	//In diesem array speichern wir die maximalen Amplituden fuer jedes Pixel...
	static float[] amplitudes = null;
	// Die positionen der Amplituden
	static int[] positions =  null;

	
	@Override
	public int[] processSeries(float[] data) {
		int[] positions =  new int[Constants.NUMBEROFPIXEL];
		int roi = data.length / Constants.NUMBEROFPIXEL;
		
		for(int pix = 0 ; pix < Constants.NUMBEROFPIXEL; pix++){
			//find max amplitude
			float max = 0;
			int posMaxAmp = 0;
			for (int slice = 0; slice < roi; slice++) {
				int pos = pix * roi + slice;
				float value = data[pos];
				if(value > max){
					max = value;
					posMaxAmp = pos; 
				}
			}
			/**
			 * original c++ code by F.Temme with some modifications
			 * one-liner comments by F.Temme  
			 */
	
			/// temp. Variables
			float           current_slope   = 0;
			float           max_slope       = 0;
			/// @todo remove magic numbers for the search window
			int             search_window_left  = posMaxAmp - 25;
			if (search_window_left < 10)
			{
				search_window_left = 10;
			}
			int             search_window_right = posMaxAmp;
			int arrivalPos = 0;
			/// Loop over all timeslices of given Window
			for( int sl = search_window_left; sl < search_window_right; sl++)
			{
				if( sl + 2 < roi) {
					current_slope              = data[sl+2] - data[sl-2];
				} else {
					break;
				}
				if (current_slope > max_slope)
				{
					max_slope       = current_slope;
					arrivalPos             = sl;
				}
			}
			positions[pix] = arrivalPos;
		}
		return positions;
	}


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
}
