/**
 *
 */
package fact.extraction.singlePulse;

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
 */
public class TimeOverThresholdArray implements Processor {
    static Logger log = LoggerFactory.getLogger(TimeOverThresholdArray.class);

    @Parameter(required = true, description = "key of data array")
    public String dataKey = null;

    @Parameter(required = true, description = "key of array containing arrival times")
    public String positionsKey = null;

    @Parameter(required = true, description = "key of output array")
    public String outputKey = null;

    @Parameter(description = "key of output for visualisation")
    public String visualizationKey = null;

    public Data process(Data item) {

        Utils.isKeyValid(item, dataKey, double[].class);
        Utils.isKeyValid(item, positionsKey, int[][].class);

        int[][] timeOverThresholdArrayList = new int[Constants.N_PIXELS][];

        double[] data = (double[]) item.get(dataKey);
        int[][] posArray = (int[][]) item.get(positionsKey);

        double[] width = new double[data.length];

        int roi = data.length / Constants.N_PIXELS;

        //Loop over pixels
        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {

            ArrayList<Integer> timeOverThresholdArray = new ArrayList<Integer>();

            //Loop over positions in positions Array
            for (int i = 0; i < posArray[pix].length; i++) {
                int position = posArray[pix][i];
                int slice = (pix * roi) + position;
                double threshold = data[slice];

                int timeOverThreshold = 0;

                //Loop over slices after arrival time and sum up those above threshold
                while (slice < data.length && threshold <= data[slice] && slice < (pix + 1) * roi) {
                    width[slice] = 10;
                    timeOverThreshold++;
                    slice++;
                    if (slice < 0 || slice > data.length) {
                        log.error(String.format("calling data array out of bounds slice = %s", slice));
                        break;
                    }
                }
                timeOverThresholdArray.add(timeOverThreshold);
            }

            timeOverThresholdArrayList[pix] = Utils.arrayListToInt(timeOverThresholdArray);
        }

        //add times over threshold array to the DataItem
        item.put(outputKey, timeOverThresholdArrayList);

        item.put(visualizationKey, width);

        return item;
    }
}
