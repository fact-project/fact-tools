/**
 * 
 */
package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import fact.Constants;

/**
 * 
 * @author Fabian Temme &lt;fabian.temme@tu-dortmund.de&gt;
 * 
 */
public class RisingEdgeForPositions implements Processor {

	static Logger log = LoggerFactory.getLogger(RisingEdgeForPositions.class);
	
	private int searchWindowLeft = 25;
	
	private String datakey = null;
	private String outputkey = null;
	private String amplitudePositionsKey = null;
	//

	
	@Override
	public Data process(Data input) {
		int[] positions =  new int[Constants.NUMBEROFPIXEL];
		
		double[] data = (double[]) input.get(datakey);		
		int[] amplitudePositions = (int[]) input.get(amplitudePositionsKey);
		
		int roi = data.length / Constants.NUMBEROFPIXEL;
		
		for(int pix = 0 ; pix < Constants.NUMBEROFPIXEL; pix++){
			int posMaxAmp = amplitudePositions[pix];
			/**
			 * original c++ code by F.Temme with some modifications
			 * one-liner comments by F.Temme  
			 */
	
			/// temp. Variables
			double           current_slope   = 0;
			double           max_slope       = 0;
			/// @todo remove magic numbers for the search window
			int             search_window_left  = posMaxAmp - searchWindowLeft;
			if (search_window_left < 10)
			{
				search_window_left = 10;
			}
			int             search_window_right = posMaxAmp;
			int arrivalPos = 0;
			/// Loop over all timeslices of given Window
			for( int slice = search_window_left; slice < search_window_right; slice++)
			{
				int pos = pix * roi + slice;
				if( slice + 2 < roi) {
					current_slope              = data[pos+2] - data[pos-2];
				} else {
					break;
				}
				if (current_slope > max_slope)
				{
					max_slope       = current_slope;
					arrivalPos             = slice;
				}
			}
			positions[pix] = arrivalPos;
		}
		input.put(outputkey, positions);
		
		return input;
		
	}


	public String getDatakey() {
		return datakey;
	}


	public void setDatakey(String datakey) {
		this.datakey = datakey;
	}


	public String getOutputKey() {
		return outputkey;
	}


	public void setOutputKey(String outputkey) {
		this.outputkey = outputkey;
	}


	public String getAmplitudePositionsKey() {
		return amplitudePositionsKey;
	}


	public void setAmplitudePositionsKey(String amplitudePositionsKey) {
		this.amplitudePositionsKey = amplitudePositionsKey;
	}


	public int getSearchWindowLeft() {
		return searchWindowLeft;
	}
	public void setSearchWindowLeft(int searchWindowLeft) {
		this.searchWindowLeft = searchWindowLeft;
	}
}
