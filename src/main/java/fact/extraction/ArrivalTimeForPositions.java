/**
 * 
 */
package fact.extraction;

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
public class ArrivalTimeForPositions implements Processor {

	static Logger log = LoggerFactory.getLogger(ArrivalTimeForPositions.class);
	
    @Parameter(required = false, defaultValue="raw:dataCalibrated")
	private String dataKey = "raw:dataCalibrated";
    @Parameter(required = false, defaultValue="pixels:maxAmplitudePositions")
	private String maxAmplitudePositionsKey = "pixels:maxAmplitudePositions";
    @Parameter(required = false, defaultValue="pixels:arrivalTimes")
	private String outputKey = "pixels:arrivalTimes";
    @Parameter(required = false, defaultValue="pixels:maxSlopes")
    private String maxSlopesKey = "pixels:maxSlopes";
    
	@Parameter(required = false, defaultValue = "25")
	private int searchWindowLeft = 25;

	private int npix;

	@Override
	public Data process(Data item) {
		Utils.isKeyValid(item, "NPIX", Integer.class);
        npix = (Integer) item.get("NPIX");
        Utils.mapContainsKeys(item, dataKey, maxAmplitudePositionsKey);

        double[] arrivalTimes =  new double[npix];
        double[] maxSlopes =  new double[npix];
		
		double[] data = (double[]) item.get(dataKey);		
		int[] maxAmplitudePositions = (int[]) item.get(maxAmplitudePositionsKey);
		
		IntervalMarker[] marker = new IntervalMarker[npix];
		
		int roi = data.length / npix;
		
		for(int pix = 0 ; pix < npix; pix++){
			int posMaxAmp = maxAmplitudePositions[pix];
	
			// temp. Variables
			double current_slope   = 0;
			double max_slope       = -Double.MAX_VALUE;
			int search_window_left  = posMaxAmp - searchWindowLeft;
			if (search_window_left < 10)
			{
				search_window_left = 10;
			}
			int             search_window_right = posMaxAmp;
			int arrivalPos = search_window_left;
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
			arrivalTimes[pix] = (double) arrivalPos;
			marker[pix] = new IntervalMarker(arrivalTimes[pix],arrivalTimes[pix] + 1);
            maxSlopes[pix] = (double) max_slope;
		}
		item.put(outputKey, arrivalTimes);
        item.put(maxSlopesKey, maxSlopes);
        item.put(outputKey + "Marker", marker);
		
		return item;
		
	}
}
