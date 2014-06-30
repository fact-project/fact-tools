/**
 * 
 */
package fact.extraction;

import java.awt.Color;

import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import stream.Data;
import stream.Processor;

/**
 * This feature is supposed to give the number of slices above a given Threshold, 
 * in order to calculate the width of a Signal
 *
 * @author <a href="mailto:jens.buss@tu-dortmund.de">Jens Buss</a> 
 *
 */
public class TimeOverThreshold implements Processor {
	static Logger log = LoggerFactory.getLogger(PhotonCharge.class);
	
	private double threshold 		= 50;
	private int skipNFirstSlices = 40;
	private int slicesAfterMaximum 	= 100;
	
	private String color = "#33CC33";
	private int alpha = 64;
	
	private String key = null;
	private String outputkey = null;
	private String positions = null;
	
	@Override
	public Data process(Data input) {
		int[] timeOverThresholdArray =  new int[Constants.NUMBEROFPIXEL];
		
		double[] data;
		try{
			data 	 = (double[]) input.get(key);
		} catch (ClassCastException e){
			log.error("Could not cast types." );
			throw e;
		}
		
		int[] posArray;
		try{
			posArray = (int[]) input.get(positions);
		} catch (ClassCastException e){
			log.error("Could not cast types." );
			throw e;
		}
		
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
		input.put("TOT_Threshold", threshold);
		
		//add number of pixel above this threshold to the DataItem
		input.put("#PixelAboveThreshold", numPixelAboveThreshold); 
				
		//add times over threshold to the DataItem
		input.put(outputkey, timeOverThresholdArray);
		input.put(outputkey+"Marker", m);
		
		//add color value if set
		if(color !=  null && !color.equals("")){
			input.put("@" + Constants.KEY_COLOR + "_"+outputkey, color);
		}

		
		return input;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public int getSkipNFirstSlices() {
		return skipNFirstSlices;
	}

	public void setSkipNFirstSlices(int skipNFirstSlices) {
		this.skipNFirstSlices = skipNFirstSlices;
	}

	public int getSlicesAfterMaximum() {
		return slicesAfterMaximum;
	}

	public void setSlicesAfterMaximum(int slicesAfterMaximum) {
		this.slicesAfterMaximum = slicesAfterMaximum;
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

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getOutputkey() {
		return outputkey;
	}

	public void setOutputkey(String outputkey) {
		this.outputkey = outputkey;
	}

	public String getPositions() {
		return positions;
	}

	public void setPositions(String positions) {
		this.positions = positions;
	}

}
