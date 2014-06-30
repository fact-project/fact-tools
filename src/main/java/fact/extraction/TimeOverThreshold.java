/**
 * 
 */
package fact.extraction;

import java.awt.Color;

import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import fact.EventUtils;
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
	static Logger log = LoggerFactory.getLogger(PhotonCharge.class);
	
	@Parameter(required = true)
	private String dataKey = null;
	@Parameter(required = true)
	private String positionsKey = null;
	@Parameter(required = true)
	private double threshold = 50;
	@Parameter(required = true)
	private String thresholdOutputKey = null;
	@Parameter(required = true)
	private String outputKey = null;
	
	private String color = "#33CC33";
	private int alpha = 64;
		
	@Override
	public Data process(Data input) {
		
		EventUtils.mapContainsKeys(getClass(), input, dataKey, positionsKey);
		
		int[] timeOverThresholdArray =  new int[Constants.NUMBEROFPIXEL];
		
		double[] data 	 = (double[]) input.get(dataKey);
		int[] posArray = (int[]) input.get(positionsKey);
		
		IntervalMarker[] m = new IntervalMarker[Constants.NUMBEROFPIXEL];
		
		Color c = Color.decode(color);
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();
		
		int roi = data.length / Constants.NUMBEROFPIXEL;
		int numPixelAboveThreshold = 0;
		
		//Loop over pixels
		for(int pix = 0 ; pix < Constants.NUMBEROFPIXEL; pix++){
			int pos = pix*roi;
			int positionOfMaximum = posArray[pix];
			
			//Check if maximum is above threshold otherwise skip the pixel
			if (data[pos + positionOfMaximum] < threshold){
				continue;
			}
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
					sl < pos + roi ; sl++)
			{			
				if (data[pos + sl] < threshold){
					lastSliceOverThresh = sl-1;
					break;
				}
				
				timeOverThreshold++;
			}


			timeOverThresholdArray[pix] = timeOverThreshold;
			m[pix] = new IntervalMarker(firstSliceOverThresh, lastSliceOverThresh, new Color(r,g,b, alpha));	
		}
		
		//add processors threshold to the DataItem
		input.put(thresholdOutputKey, threshold);
		
		//add number of pixel above this threshold to the DataItem
		input.put(outputKey+"_numPixel", numPixelAboveThreshold); 
				
		//add times over threshold to the DataItem
		input.put(outputKey, timeOverThresholdArray);
		input.put(outputKey+"Marker", m);
		
		//add color value if set
		if(color !=  null && !color.equals("")){
			input.put("@" + Constants.KEY_COLOR + "_"+outputKey, color);
		}

		return input;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
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
	
	

}
