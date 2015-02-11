/**
 * 
 */
package fact.extraction;

import fact.Utils;
import fact.hexmap.ui.overlays.PixelSetOverlay;

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
	
	@Parameter(required = true)
	private String dataKey = null;
	@Parameter(required = true)
	private String positionsKey = null;
	@Parameter(required = true)
	private double threshold = 50;
	@Parameter(required = true)
	private String thresholdOutputKey = null;
	@Parameter(required = true)
	private String firstSliceOverThresholdOutputKey = null;
	@Parameter(required = true)
	private String outputKey = null;
	
	private PixelSetOverlay pixelSet;
	
	private int npix;

	public Data process(Data input) {
        Utils.isKeyValid(input, dataKey, double[].class);
		Utils.isKeyValid(input, positionsKey, int[].class);
		Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");
				
		int[] timeOverThresholdArray =  new int[npix];
		double[] firstSliceOverThresholdArray =  new double[npix];
		
		double[] data 	 = (double[]) input.get(dataKey);
		int[] posArray = (int[]) input.get(positionsKey);
		
		IntervalMarker[] m = new IntervalMarker[npix];
			
		int roi = data.length / npix;
		int numPixelAboveThreshold = 0;
		
		pixelSet = new PixelSetOverlay();
        int[] totPixelSet = null;

		//Loop over pixels
		for(int pix = 0 ; pix < npix; pix++){
			firstSliceOverThresholdArray[pix] = 0;
			
			int pos = pix*roi;
			int positionOfMaximum = posArray[pix];
			
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


			timeOverThresholdArray[pix] = timeOverThreshold;
			firstSliceOverThresholdArray[pix] = (double) firstSliceOverThresh;
			m[pix] = new IntervalMarker(firstSliceOverThresh, lastSliceOverThresh);	
		}
		
	
		
		//add processors threshold to the DataItem
		input.put(thresholdOutputKey, threshold);
		
		//add number of pixel above this threshold to the DataItem
		input.put(outputKey+"_numPixel", numPixelAboveThreshold); 
				
		//add times over threshold to the DataItem
		input.put(outputKey, timeOverThresholdArray);
		input.put(firstSliceOverThresholdOutputKey, firstSliceOverThresholdArray);
		input.put(outputKey+"Marker", m);
		input.put(outputKey+"SetOverlay", pixelSet);

        //Add totPixelSet only to data item if it is not empty
        totPixelSet = pixelSet.toIntArray();
        if (totPixelSet.length != 0){
            input.put(outputKey+"Set", pixelSet.toIntArray());
        }
        return input;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
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

	public String getPositionsKey() {
		return positionsKey;
	}

	public void setPositionsKey(String positionsKey) {
		this.positionsKey = positionsKey;
	}

	public String getThresholdOutputKey() {
		return thresholdOutputKey;
	}

	public void setThresholdOutputKey(String thresholdOutputKey) {
		this.thresholdOutputKey = thresholdOutputKey;
	}

	public String getFirstSliceOverThresholdOutputKey() {
		return firstSliceOverThresholdOutputKey;
	}

	public void setFirstSliceOverThresholdOutputKey(
			String firstSliceOverThresholdOutputKey) {
		this.firstSliceOverThresholdOutputKey = firstSliceOverThresholdOutputKey;
	}
	
	

}
