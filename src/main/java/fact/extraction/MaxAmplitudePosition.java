/**
 * 
 */
package fact.extraction;

import fact.Utils;

import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This processor simply calculates the position of the maximum value for all time slices in each Pixel.
 * outputs an int array  
 * 
 *@author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class MaxAmplitudePosition implements Processor {
	static Logger log = LoggerFactory.getLogger(MaxAmplitudePosition.class);

    @Parameter(required = true)
    private String key;

    @Parameter(required = true)
    private String outputKey;


	private Integer searchWindowLeft = null;
	private Integer searchWindowRight =  null;

	private int npix;


	@Override
	public Data process(Data input) {
        Utils.isKeyValid(input, key, double[].class);
		Utils.isKeyValid(input, "NPIX", Integer.class);
        double[] data = (double[]) input.get(key);
        npix = (Integer) input.get("NPIX");	
        int roi = data.length / npix;

        int[] positions =  new int[npix];
        
        IntervalMarker[] m = new IntervalMarker[npix];

		if (searchWindowLeft == null || searchWindowLeft < 0){
			searchWindowLeft = 0;
		}
		if (searchWindowRight == null || searchWindowRight > roi){
			searchWindowRight = roi;
		}
		//foreach pixel
		for (int pix = 0; pix < npix; pix++) {
			positions[pix] = findMaximumPosition(pix, roi, data);
			m[pix] = new IntervalMarker(positions[pix],positions[pix] + 1);
		}
        input.put(outputKey, positions);
        input.put(outputKey + "Marker", m);
		return input;
	}

    /**
     * finds the position of the highest value in the array. If the maximum value is not unique the first position will be used
     * @param pix Pixel to check
     * @param roi Basically the number of slices in one event
     * @param data the array which to check
     * @return
     */
    public int findMaximumPosition(int pix, int roi, double[] data){
        //the first value we find is the current maximum
        double tempMaxValue = data[pix*roi+searchWindowLeft];
        int position = searchWindowLeft;
        //iterate over all slices
        for (int slice = searchWindowLeft; slice < searchWindowRight; slice++) {
            int pos = pix * roi + slice;
            //temp save the current value
            double value = data[pos];
            //update maxvalue and position if current value exceeds old value
            if( value > tempMaxValue){
                tempMaxValue = value;
                position = slice;
            }
        }
        return position;
    }

	/*
	 * Getter and Setter
	 */
	public void setSearchWindowLeft(int searchWindowLeft) {
		this.searchWindowLeft = searchWindowLeft;
	}

	public void setSearchWindowRight(int searchWindowRight) {
		this.searchWindowRight = searchWindowRight;
	}

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }
    public String getOutputKey() {
        return outputKey;
    }



}
