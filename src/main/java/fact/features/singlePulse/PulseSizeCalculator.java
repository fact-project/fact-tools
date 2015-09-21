/**
 * 
 */
package fact.features.singlePulse;

import fact.Constants;
import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.ArrayList;

/**
 * Finds sum of slice amplitudes starting at pulse arrival time 
 * 
 *@author Katie Gray &lt;kathryn.gray@tu-dortmund.de&gt;
 * 
 */
public class PulseSizeCalculator implements Processor {
    static Logger log = LoggerFactory.getLogger(PulseSizeCalculator.class);

    @Parameter(required = true)
    private String key;
    @Parameter(required = true)
    private String outputKey;
        //size of pulse
    @Parameter(required = true)
    private String arrivalTimeKey;
        //positions of arrival times 
    @Parameter(required = true)
    private int width;
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
	    double[][] pulseSizes = new double[npix][];

		//for each pixel
		for (int pix = 0; pix < npix; pix++) {
			pulseSizes[pix] = new double[arrivalTimes[pix].length];
			pulseSizes[pix] = calculateSizes(pix, roi, data, arrivalTimes);
		}
        input.put(outputKey, pulseSizes);

        
//       System.out.println(Arrays.toString(pulseSizes));
//       System.out.println(singlePixelPulses);

        return input;
    }

    /**
     * @param pix Pixel to check
     * @param roi Basically the number of slices in one event
     * @param data the array which to check
     * @return
     */
	
    public double[] calculateSizes(int pix, int roi, double[] data, int[][] arrivalTimes){
      
		ArrayList<Double> sizes = new ArrayList<Double>();
    	
        if(arrivalTimes[pix].length > 0){
        	int numberPulses = arrivalTimes[pix].length;
        	for(int i = 0; i < numberPulses; i++){
				double integral = 0;
				int start = arrivalTimes[pix][i];
				for(int slice = start; slice < start + width; slice++){
					int pos = pix * roi + slice;
					integral += data[pos];
				}
				sizes.add(integral);
        	}		
        }
        return Utils.arrayListToDouble(sizes);
    }
          
     
    /*
     * Getters and Setters
     */


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

    public String getarrivalTimeKey() {
        return arrivalTimeKey;
    }

    public void setarrivalTimeKey(String arrivalTimeKey) {
        this.arrivalTimeKey = arrivalTimeKey;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

}