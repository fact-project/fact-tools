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
 * This feature is supposed to give the number of slices after above a certain Threshold in order to calculate the width
 * of a Signal. The Threshold is given by the Amplitude in a given start slice (Arrival Time) . The Processor can get an
 * array of arrival times.
 *
 * @author <a href="mailto:jens.buss@tu-dortmund.de">Jens Buss</a> 
 *
 */
public class TimeOverThresholdArray implements Processor {
	static Logger log = LoggerFactory.getLogger(TimeOverThresholdArray.class);
	
	@Parameter(required = true, description = "key of data array")
	private String dataKey = null;
	@Parameter(required = true, description = "key of array containing arrival times")
	private String positionsKey = null;
	@Parameter(required = true, description = "key of output array")
	private String outputKey = null;
    @Parameter(description = "key of output for visualisation")
    private String visualizationKey = null;

	public Data process(Data input) {
		
		Utils.isKeyValid(input, dataKey, double[].class);
		Utils.isKeyValid(input, positionsKey, ArrayList[].class);

        ArrayList[] timeOverThresholdArrayList = new ArrayList[Constants.NUMBEROFPIXEL];

		double[] data 	 = (double[]) input.get(dataKey);
        ArrayList[] posArray = (  ArrayList[] ) input.get(positionsKey);

        double[] width 	 =  new double[data.length];

		int roi = data.length / Constants.NUMBEROFPIXEL;

		//Loop over pixels
		for(int pix = 0 ; pix < Constants.NUMBEROFPIXEL; pix++){

            ArrayList<Integer>  timeOverThresholdArray =  new ArrayList<Integer>();

            //Loop over positions in positions Array
            for (int i = 0 ; i < posArray[pix].size() ; i++ ){
                int     position    = (Integer) posArray[pix].get(i);
                int     slice       = (pix * roi) + position;
                double  threshold   = data[slice];

                int timeOverThreshold = 0;

                //Loop over slices after arrival time and sum up those above threshold
                while (slice < data.length && threshold <= data[slice] && slice < (pix+1)*roi ){
                    width[slice] = 10;
                    timeOverThreshold++;
                    slice++;
                    if (slice < 0 || slice > data.length){
                        log.error(String.format("calling data array out of bounds slice = %s", slice));
                        break;
                    }
                }
                timeOverThresholdArray.add(timeOverThreshold);
			}

            timeOverThresholdArrayList[pix] =  timeOverThresholdArray;
		}

		//add times over threshold array to the DataItem
		input.put(outputKey, timeOverThresholdArrayList);

        input.put(visualizationKey, width);

		return input;
	}

    public String getDataKey() {
        return dataKey;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public String getPositionsKey() {
        return positionsKey;
    }

    public void setPositionsKey(String positionsKey) {
        this.positionsKey = positionsKey;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public String getVisualizationKey() {
        return visualizationKey;
    }

    public void setVisualizationKey(String visualizationKey) {
        this.visualizationKey = visualizationKey;
    }
}
