
package fact.processors.parfact;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import fact.Constants;
import fact.data.MaxAmplitudePosition;

/**
 * This processor Calculates PhotonCharge by doing the following: 
 * 1. 	Use the MaxAmplitude Processor to find the maximum Value in the slices.
 * 2.	In the area between amplitudePosition...amplitudePositon-25 search for the position having 0.5 of the original maxAmplitude.
 * 3.	Now for some reason sum up all slices between half_max_pos and  half_max_pos + 30.
 * 4. 	Divide the sum by the integralGain and save the result.
 * 
 * Treatment of edge Cases is currently very arbitrary since Pixels with these values should not be considered as showerPixels anyways.
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 *
 */
public class CalculatePhotonCharge implements Processor {
	static Logger log = LoggerFactory.getLogger(CalculatePhotonCharge.class);
	float[] photonCharge = null;
	float[] arrivalTime = null;
	private String output = null;

	private String key;
	private	double average = 0.0;
	private float integralGain = 244.0f;

	public CalculatePhotonCharge(){
	}
	public CalculatePhotonCharge(String key){
		this.key =  key;
	}
	
	@Override
	public Data process(Data input) {
		
		processEvent(input, key);
		if(output == null || output ==""){
			input.put(Constants.KEY_PHOTONCHARGE+"_average",average);
			input.put(Constants.KEY_PHOTONCHARGE, photonCharge);
		} else {
			input.put(output+"_average",average);
			input.put(output, photonCharge);
		}
		return input;
	}
	
	public float[] processEvent(Data input, String key) {
		
		Serializable value = null;
		if(input.containsKey(key)){
			 value = input.get(key);
		} else {
			log.info(Constants.ERROR_WRONG_KEY + key + ",  " + this.getClass().getSimpleName() );
			return null;
		}
		//Check if value is of the right type
		if (value != null && value.getClass().isArray()
				&& value.getClass().getComponentType().equals(float.class)) {
			return processSeries((float[]) value);
			
		}
		else 
		{
			log.info(Constants.EXPECT_ARRAY_F + key + ",  " + this.getClass().getSimpleName() );
			return null;
		}
	
	}
	public float[] processSeries(float[] value) {
		photonCharge = new float[Constants.NUMBEROFPIXEL];
		
		float[] data = value;
		int[] positions = new MaxAmplitudePosition().processSeries(data);
//		mA = null;
		int roi = data.length / Constants.NUMBEROFPIXEL;
		
		// for each pixel
		for(int pix = 0 ; pix < Constants.NUMBEROFPIXEL; pix++){
    	
		    	/**
		    	 * watch out. index can get out of bounds!
		    	 */
				int pos = pix*roi;
			    int positionOfMaximum                 = positions[pix];
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
		return photonCharge;
	}
	
	
	/*Getters and Setters */
	
	public float getIntegralGain() {
		return integralGain;
	}
	@Parameter(required = false, description = "Value for the integral Gain. This is a measured Constant.", defaultValue = "244.0")
	public void setIntegralGain(float integralGain) {
		this.integralGain = integralGain;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	

	public String getOutput() {
		return output;
	}
	public void setOutput(String output) {
		this.output = output;
	}
	
	
	
}
