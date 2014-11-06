/**
 * 
 */
package fact.extraction;

import fact.Constants;
import fact.Utils;

import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * 
 * @author Fabian Temme &lt;fabian.temme@tu-dortmund.de&gt;
 * 
 */
public class RisingEdgeForPositions implements Processor {

	static Logger log = LoggerFactory.getLogger(RisingEdgeForPositions.class);
	
	private int searchWindowLeft = 25;

    @Parameter(required = true)
	private String dataKey = null;
    @Parameter(required = true)
	private String outputKey = null;
    @Parameter(required = true)
	private String amplitudePositionsKey = null;
    @Parameter(required = true)
    private String maxSlopesKey = null;

	@Override
	public Data process(Data input) {
        Utils.mapContainsKeys(input, dataKey, amplitudePositionsKey);

        double[] positions =  new double[Constants.NUMBEROFPIXEL];
        double[] maxSlopes =  new double[Constants.NUMBEROFPIXEL];
		
		double[] data = (double[]) input.get(dataKey);		
		int[] amplitudePositions = (int[]) input.get(amplitudePositionsKey);
		
		IntervalMarker[] m = new IntervalMarker[Constants.NUMBEROFPIXEL];
		
		int roi = data.length / Constants.NUMBEROFPIXEL;
		
		for(int pix = 0 ; pix < Constants.NUMBEROFPIXEL; pix++){
			int posMaxAmp = amplitudePositions[pix];
	
			// temp. Variables
			double           current_slope   = 0;
			double           max_slope       = 0;
			int             search_window_left  = posMaxAmp - searchWindowLeft;
			if (search_window_left < 10)
			{
				search_window_left = 10;
			}
			int             search_window_right = posMaxAmp;
			int arrivalPos = 0;
			// Loop over all timeslices of given window
			// check for the largest derivation over 5 slices
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
					max_slope = current_slope;
					arrivalPos = slice;
				}
			}
			positions[pix] = (double) arrivalPos;
			m[pix] = new IntervalMarker(positions[pix],positions[pix] + 1);
            maxSlopes[pix] = (double) max_slope;
		}
		input.put(outputKey, positions);
        input.put(maxSlopesKey, maxSlopes);
        input.put(outputKey + "Marker", m);
		
		return input;
		
	}

	public String getDataKey() {
		return dataKey;
	}

	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}
	
	public String getAmplitudePositionsKey() {
		return amplitudePositionsKey;
	}

	public void setAmplitudePositionsKey(String amplitudePositionsKey) {
		this.amplitudePositionsKey = amplitudePositionsKey;
	}

    public String getMaxSlopesKey() {
        return maxSlopesKey;
    }

    public void setMaxSlopesKey(String maxSlopesKey) {
        this.maxSlopesKey = maxSlopesKey;
    }

    public int getSearchWindowLeft() {
		return searchWindowLeft;
	}
	public void setSearchWindowLeft(int searchWindowLeft) {
		this.searchWindowLeft = searchWindowLeft;
	}
}
