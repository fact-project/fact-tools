
package fact.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.Constants;

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
public class CalculatePhotonCharge implements Processor {
	static Logger log = LoggerFactory.getLogger(CalculatePhotonCharge.class);
	private float[] photonCharge = null;

	private	double average = 0.0;
	private float integralGain = 244.0f;

	private String positions = null;
	private String key = "DataCalibrated";
	private String outputKey = key;
	
	@Override
	public Data process(Data input) {
		int[] posArray = null;
		float[] data = null;
		try{
			data = (float[]) input.get(key);
			if (data == null){
				throw new Exception();
			}
			if(positions != null){
				posArray=(int[]) input.get(positions);
			} else {
				posArray=new MaxAmplitudePosition().processSeries(data);
			}
		} catch (ClassCastException e){
			log.error("Could not cast data to float[]   "+ key);
		} catch(Exception e){
			log.error("Could not get the right items from the map. wrong key?   " + key );
		}
		
		photonCharge = new float[Constants.NUMBEROFPIXEL];
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
			    	for (int sl = positionOfHalfMaximumValue ; sl < positionOfHalfMaximumValue + 30 ; sl++)  integral += data[sl + (pix*roi)];
			    } else {
			    	integral = data[positionOfMaximum +pix*roi] / 9.0f;
			    }			    
			    photonCharge[pix] = integral/integralGain;
			   	average += photonCharge[pix];
			
		}
		average = average/Constants.NUMBEROFPIXEL;  
		input.put(outputKey, photonCharge);
		return input;
	}

	
	/*Getters and Setters */
	
	public float getIntegralGain() {
		return integralGain;
	}
	@Parameter(required = false, description = "Value for the integral Gain. This is a measured Constant.", defaultValue = "244.0")
	public void setIntegralGain(float integralGain) {
		this.integralGain = integralGain;
	}


	@Parameter(required = false, description = "The positions from which to integrate. If no input is given here this processor will use the posititions of the max amplitude", defaultValue = "MaxAmplitudePositons")
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

}
