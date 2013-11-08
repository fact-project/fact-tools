
package fact.features;

import java.awt.Color;

import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.Constants;
import fact.EventUtils;

/**
 * This processor Calculates PhotonCharge by doing the following: 
 * 1. 	Use the MaxAmplitude Processor to find the maximum Value in the slices.</br>
 * 2.	In the area between amplitudePosition...amplitudePositon-25 search for the position having 0.5 of the original maxAmplitude.</br>
 * 3.	Now for some reason sum up all slices between half_max_pos and  half_max_pos + 30.</br>
 * 4. 	Divide the sum by the integralGain and save the result.</br>
 * 
 * Treatment of edge Cases is currently very arbitrary since Pixels with these values should not be considered as showerPixels anyways.
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 *
 */
public class PhotonCharge implements Processor {
	static Logger log = LoggerFactory.getLogger(PhotonCharge.class);
	private double[] photonCharge = null;

	private String color = "#00F0F0";

	private	double average = 0.0;
	private double integralGain = 244.0f;
	private int alpha = 64;

	private String positions = null;

	private String key = "DataCalibrated";
	private String outputKey = key;

	@Override
	public Data process(Data input) {
		EventUtils.mapContainsKeys(getClass(), input, positions, key);
		int[] posArray;
		double[] data;
		try{
			posArray = (int[]) input.get(positions);
			data = (double[]) input.get(key);
		} catch (ClassCastException e){
			log.error("Could not cast types." );
			throw e;
		}
		
		
		IntervalMarker[] m = new IntervalMarker[Constants.NUMBEROFPIXEL];
		photonCharge = new double[Constants.NUMBEROFPIXEL];
		int roi = data.length / Constants.NUMBEROFPIXEL;
		// for each pixel
		for(int pix = 0 ; pix < Constants.NUMBEROFPIXEL; pix++){

			/**
			 * watch out. index can get out of bounds!
			 */
			int pos = pix*roi;
			int positionOfMaximum                 = posArray[pix];
			int positionOfHalfMaximumValue            = 0;
			if(positionOfMaximum <=25){
				positionOfMaximum=25;
			}
			//in an area of amplitudePosition...amplitudePositon-25 search for the postion having 0.5of the original maxAmplitude
			for (int sl = positionOfMaximum ; sl > positionOfMaximum - 25 ; sl--)
			{
				positionOfHalfMaximumValue        = sl;

				if (data[pos + sl-1] < data[pos + positionOfMaximum] / 2  && data[pos + sl] >= data[pos + positionOfMaximum] / 2)
				{
					break;
				}
			}

			float integral              = 0;
			//and now for some reason sum up all slices between half_max_pos and  half_max_pos + 30.
			//watch out for right margin of array here
			if(positionOfHalfMaximumValue + 30 < roi ){
				for (int sl = positionOfHalfMaximumValue ; sl < positionOfHalfMaximumValue + 30 ; sl++){  
					integral += data[sl + (pix*roi)];
				}
			}
			else {
				integral = 0;
			}			    
			photonCharge[pix] = integral/integralGain;
			average += photonCharge[pix];
			Color c = Color.decode(color);
			int r = c.getRed();
			int g = c.getGreen();
			int b = c.getBlue();

			m[pix] = new IntervalMarker(positionOfHalfMaximumValue, positionOfHalfMaximumValue + 30, new Color(r,g,b, alpha));
		}
		average = average/Constants.NUMBEROFPIXEL;

		//add color value if set
		input.put(outputKey+"Marker", m);
		if(color !=  null && !color.equals("")){
			input.put("@" + Constants.KEY_COLOR + "_"+outputKey+"Marker", color);
		}		
		input.put(outputKey, photonCharge);
		return input;
	}


	/*Getters and Setters */

	public double getIntegralGain() {
		return integralGain;
	}
	@Parameter(required = false, description = "Value for the integral Gain. This is a measured Constant.", defaultValue = "244.0")
	public void setIntegralGain(float integralGain) {
		this.integralGain = integralGain;
	}


	public String getPositions() {
		return positions;
	}
	@Parameter(required = false, description = "The positions from which to integrate.", defaultValue = "positions")
	public void setPositions(String positions) {
		this.positions = positions;
	}

	
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}



	public String getOutputKey() {
		return outputKey;
	}
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
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

}
