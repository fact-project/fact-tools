/**
 * 
 */
package fact.features.singlePulse;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;

/**
 * Calculates the slope of each identified pulse in the time series
 * 
 *@author Jens Buss &lt;jens.buss@tu-dortmund.de&gt;
 * 
 */
public class PulseSlopeCalculator implements Processor {
    static Logger log = LoggerFactory.getLogger(PulseSlopeCalculator.class);

    @Parameter(required = true)
    private String key;
    @Parameter(required = true)
    private String outputKey;
        //size of pulse
    @Parameter(required = true)
    private String arrivalTimeKey;
        //positions of arrival times 
    @Parameter()
    private int width = 2;
    	//number of slices over which we integrate

    private int npix;

	@Override
	public Data process(Data input) {
        Utils.mapContainsKeys(input, key, arrivalTimeKey);
        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");

        double[] data = (double[]) input.get(key);
        int roi = data.length / npix;
		int[][] arrivalTimes = (int[][]) input.get(arrivalTimeKey);
	    double[][] pulseSlopes = new double[npix][];

		//for each pixel
		for (int pix = 0; pix < npix; pix++) {
			pulseSlopes[pix] = new double[arrivalTimes[pix].length];
			pulseSlopes[pix] = calculateSlopes(pix, roi, width, data, arrivalTimes);
		}
        input.put(outputKey, pulseSlopes);

        return input;
    }

    /**
     * @param pix Pixel to check
     * @param roi Basically the number of slices in one event
     * @param data the array which to check
     * @return
     */
	
    public double[] calculateSlopes(int pix, int roi, int width, double[] data, int[][] arrivalTimes){
      
		ArrayList<Double> slopes = new ArrayList<Double>();
    	
        if(arrivalTimes[pix].length <= 0){
            return Utils.arrayListToDouble(slopes);
        }

        for(int i = 0; i < arrivalTimes[pix].length; i++){
            int start = arrivalTimes[pix][i];

            // check if slices are in roi
            if(start + width > roi || start - width < 0){
                continue;
            }

            int high_pos = pix * roi + start + width;
            int low_pos = pix * roi + start - width;

            slopes.add( (data[high_pos] - data[low_pos]) / (2*width) );
        }

        return Utils.arrayListToDouble(slopes);
    }
          
     
    /*
     * Getters and Setters
     */


    public void setKey(String key) {
        this.key = key;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public void setarrivalTimeKey(String arrivalTimeKey) {
        this.arrivalTimeKey = arrivalTimeKey;
    }

    public void setWidth(int width) {
        this.width = width;
    }

}
