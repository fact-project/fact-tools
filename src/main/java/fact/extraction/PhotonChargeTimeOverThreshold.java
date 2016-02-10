/**
 * 
 */
package fact.extraction;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This feature is supposed to calculate the photon charge of a SiPM pulse 
 * from number of slices above a given Threshold (aka Pulse Width). So far this feature
 * is tuned to the pulse shape (defined by the used SiPMs and electronics)
 * as it is produced by FACT
 *
 * @author <a href="mailto:jens.buss@tu-dortmund.de">Jens Buss</a> 
 *
 */
@Deprecated
public class PhotonChargeTimeOverThreshold implements Processor {
	static Logger log = LoggerFactory.getLogger(PhotonChargeTimeOverThreshold.class);
	
	@Parameter(required=true, description="")
	private String timeOverThresholdKey = null;
	@Parameter(required=true, description="")
	private String thresholdKey = null;
	@Parameter(required=true)
	private String outputKey = null;
	
	private double threshold = 0;
	private int npix;

	public Data process(Data input) {
		Utils.isKeyValid(input, timeOverThresholdKey, int[].class);
		Utils.isKeyValid(input, thresholdKey, Double.class);
		Utils.isKeyValid(input, "NPIX", Integer.class);
		npix = (Integer) input.get("NPIX");
				
		
		double[] chargeFromThresholdArray =  new double[npix];
				
		int[] timeOverThresholdArray 	 = (int[]) input.get(timeOverThresholdKey);
		threshold = (Double) input.get(thresholdKey);
				
		for(int pix = 0 ; pix < npix; pix++){
			
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
		input.put(outputKey, chargeFromThresholdArray);
		
		return input;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
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
