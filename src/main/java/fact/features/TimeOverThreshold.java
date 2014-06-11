/**
 * 
 */
package fact.features;

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
	
	private String color = "#00F0F0";
	private int alpha = 64;
	
	private String key = null;
	private String outputkey = null;
	private String positions = null;
	
	@Override
	public Data process(Data input) {
		double[] timeOverThresholdArray =  new double[Constants.NUMBEROFPIXEL];
		
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
		
		for(int pix = 0 ; pix < Constants.NUMBEROFPIXEL; pix++){
			int pos = pix*roi;
			int positionOfMaximum = posArray[pix];

//			if(positionOfMaximum <=25){
//				positionOfMaximum=25;
//			}
			
			int timeOverThreshold = 0;
			int firstSliceOverThresh = 0;
			for (int sl = positionOfMaximum - slicesBeforeMaximum ; 
					sl < positionOfMaximum + slicesAfterMaximum ; sl++)
			{	
				int currentPos = pos + sl-1;
				if (sl < 0){
					sl = 0;
				}
				
				if (sl > roi){
					timeOverThreshold = 0;
					break;
				}
				
				if (data[currentPos] > threshold){
					timeOverThreshold++;
					if (firstSliceOverThresh == 0){
						firstSliceOverThresh = sl;
					}
				}
				
				if (data[currentPos] < threshold && currentPos > positionOfMaximum){
					break;
				}
				
			}
			timeOverThresholdArray[pix] = timeOverThreshold;
			m[pix] = new IntervalMarker(firstSliceOverThresh, firstSliceOverThresh + timeOverThreshold, new Color(r,g,b, alpha));
		}
		//add times over threshold
		input.put(outputkey, timeOverThresholdArray);
		
		//add color value if set
		input.put(outputkey+"Marker", m);
		if(color !=  null && !color.equals("")){
			input.put("@" + Constants.KEY_COLOR + "_"+outputkey+"Marker", color);
		}
		
		return input;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public int getSlicesBeforeMaximum() {
		return slicesBeforeMaximum;
	}

	public void setSlicesBeforeMaximum(int slicesBeforeMaximum) {
		this.slicesBeforeMaximum = slicesBeforeMaximum;
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
