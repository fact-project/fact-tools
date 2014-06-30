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
 * This feature is supposed to calculate the photon charge of a SiPM pulse 
 * from number of slices above a given Threshold (aka Pulse Width). So far this feature
 * is tuned to the pulse shape (defined by the used SiPMs and electronics)
 * as it is produced by FACT
 *
 * @author <a href="mailto:jens.buss@tu-dortmund.de">Jens Buss</a> 
 *
 */
public class PhotonChargeTimeOverTreshold implements Processor {
	static Logger log = LoggerFactory.getLogger(PhotonCharge.class);
	
	private double threshold = 0;
	private String key = null;
	private String outputkey = null;

	/* (non-Javadoc)
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {
		double[] chargeFromThresholdArray =  new double[Constants.NUMBEROFPIXEL];
		
		
		int[] timeOverThresholdArray;
		try{
			timeOverThresholdArray 	 = (int[]) input.get(key);
		} catch (ClassCastException e){
			log.error("Could not cast types." );
			throw e;
		}
		
		for(int pix = 0 ; pix < Constants.NUMBEROFPIXEL; pix++){
			
			chargeFromThresholdArray[pix] = 0.;
					
			// validate parameters
		    if(timeOverThresholdArray[pix] <= 0){
		    	continue;
		    }
		    if(threshold == 0){
		    	continue;
		    }

		    // ATTENTION: the following are MAGIC NUMBERS to define a function
		    // that computes the charge from a pulses width @ a threshold of 1500 mV
		    // this is a dirty Hack to check if saturated pixels can be reconstructed
		    double par[] = {
		                    -1.83*Math.pow(10, -6),
		                    0.027,
		                    0.0009,
		                    3.54
		                    };

		    double charge =par[2] * threshold + par[3];
		    charge += (par[0] * threshold + par[1]) * timeOverThresholdArray[pix];
		    charge = Math.exp(charge);
		    
		    chargeFromThresholdArray[pix] = charge + 40;
		}
		
		//add times over threshold
		input.put(outputkey, chargeFromThresholdArray);
		
		return input;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public String getOutputkey() {
		return outputkey;
	}

	public void setOutputkey(String outputkey) {
		this.outputkey = outputkey;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
