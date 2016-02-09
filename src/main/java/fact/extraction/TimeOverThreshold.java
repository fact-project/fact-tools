/**
 * 
 */
package fact.extraction;

import fact.Utils;
import fact.container.PixelSet;
import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This feature is supposed to give the number of slices above a given Threshold, 
 * in order to calculate the width of a Signal
 *
 * @author <a href="mailto:jens.buss@tu-dortmund.de">Jens Buss</a> 
 *
 */
public class TimeOverThreshold implements Processor {
	static Logger log = LoggerFactory.getLogger(TimeOverThreshold.class);
	
	@Parameter(required = false, defaultValue="raw:dataCalibrated")
	private String dataKey = "raw:dataCalibrated";
	@Parameter(required = false)
	private String maxAmplitudePositionsKey = "pixels:maxAmplitudePositions";
	@Parameter(required = false, defaultValue="1800")
	private double threshold = 1800;
	@Parameter(required = false, defaultValue="pixels:firstSliceOverThresholds")
	private String firstSliceOverThresholdsKey = "pixels:firstSliceOverThresholds";
	@Parameter(required = false, defaultValue="pixels:timeOverThresholds")
	private String outputKey = "pixels:timeOverThresholds";
	@Parameter(required = false, defaultValue = "meta:timeOverThreshold:threshold")
	private String thresholdKey = "meta:timeOverThresholdProcessor:threshold";
	
	private PixelSet pixelSet;
	
	private int npix;

	public Data process(Data item) {
        Utils.isKeyValid(item, dataKey, double[].class);
		Utils.isKeyValid(item, maxAmplitudePositionsKey, int[].class);
		Utils.isKeyValid(item, "NPIX", Integer.class);
        npix = (Integer) item.get("NPIX");
				
		int[] timeOverThresholds =  new int[npix];
		double[] firstSliceOverThresholds =  new double[npix];
		
		double[] data 	 = (double[]) item.get(dataKey);
		int[] maxAmplitudePositions = (int[]) item.get(maxAmplitudePositionsKey);
		
		IntervalMarker[] marker = new IntervalMarker[npix];
			
		int roi = data.length / npix;
		int numPixelAboveThreshold = 0;
		
		pixelSet = new PixelSet();

		//Loop over pixels
		for(int pix = 0 ; pix < npix; pix++){
			firstSliceOverThresholds[pix] = 0;
			
			int pos = pix*roi;
			int positionOfMaximum = maxAmplitudePositions[pix];
			
			//Check if maximum is above threshold otherwise skip the pixel
			if (data[pos + positionOfMaximum] < threshold){
				continue;
			}
			pixelSet.addById(pix);
			numPixelAboveThreshold++;
			
			int timeOverThreshold = 0;
			int firstSliceOverThresh = 0;
			int lastSliceOverThresh  = 0;
			
			//Loop over slices before Maximum and sum up those above threshold
			for (int sl = positionOfMaximum ; 
					sl > 0 ; sl--)
			{					
				if (data[pos + sl] < threshold){
					firstSliceOverThresh = sl+1;
					break;
				}		
				
				timeOverThreshold++;
			}
			
			//Loop over slices after Maximum and sum up those above threshold
			for (int sl = positionOfMaximum + 1 ; 
					sl < roi ; sl++)
			{			
				if (data[pos + sl] < threshold){
					lastSliceOverThresh = sl-1;
					break;
				}
				
				timeOverThreshold++;
			}


			timeOverThresholds[pix] = timeOverThreshold;
			firstSliceOverThresholds[pix] = (double) firstSliceOverThresh;
			marker[pix] = new IntervalMarker(firstSliceOverThresh, lastSliceOverThresh);	
		}
		
		item.put(thresholdKey,threshold);
		
		//add number of pixel above this threshold to the DataItem
		item.put(outputKey+":numPixels", numPixelAboveThreshold); 
				
		//add times over threshold to the DataItem
		item.put(outputKey, timeOverThresholds);
		item.put(firstSliceOverThresholdsKey, firstSliceOverThresholds);
		item.put(outputKey+"Marker", marker);
		item.put(outputKey+"SetOverlay", pixelSet);
        return item;
	}
	

}
