/**
 * 
 */
package fact.extraction;

import java.awt.Color;

import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import fact.Utils;
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
public class PhotonChargeTimeOverThreshold implements Processor {
	static Logger log = LoggerFactory.getLogger(PhotonCharge.class);
	

	private String timeOverThresholdKey = null;
	private String thresholdKey = null;
	private String outputkey = null;
	
	private double threshold = 0;

	/* (non-Javadoc)
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(Data input) {
		Utils.mapContainsKeys(getClass(), input, timeOverThresholdKey,thresholdKey);
		
		
		double[] chargeFromThresholdArray =  new double[Constants.NUMBEROFPIXEL];
		
		
		int[] timeOverThresholdArray 	 = (int[]) input.get(timeOverThresholdKey);
		threshold = (Double) input.get(thresholdKey);
		
		if (threshold != 1500)
		{
			throw new RuntimeException("Currently only threshold equal 1500 mV supported");
		}
		
		
		for(int pix = 0 ; pix < Constants.NUMBEROFPIXEL; pix++){
			
			chargeFromThresholdArray[pix] = 0.;
					
			// validate parameters
		    if(timeOverThresholdArray[pix] <= 0){
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

	public String getOutputkey() {
		return outputkey;
	}

	public void setOutputkey(String outputkey) {
		this.outputkey = outputkey;
	}

	public String getTimeOverThresholdKey() {
		return timeOverThresholdKey;
	}

	public void setTimeOverThresholdKey(String timeOverThresholdKey) {
		this.timeOverThresholdKey = timeOverThresholdKey;
	}

	public String getThresholdKey() {
		return thresholdKey;
	}

	public void setThresholdKey(String thresholdKey) {
		this.thresholdKey = thresholdKey;
	}
	
	


}
