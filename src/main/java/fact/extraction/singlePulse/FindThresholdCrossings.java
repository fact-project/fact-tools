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
 * Calculates the positions of pulse candidates by finding threshold crossings with boundary restrictions.
 * Outputs an array containing a list of positions for each pixel.
 *
 * @author Katie Gray &lt;kathryn.gray@tu-dortmund.de&gt;
 */

public class FindThresholdCrossings implements Processor {
    private static final Logger log = LoggerFactory.getLogger(FindThresholdCrossings.class);

    @Parameter(required = true)
    public String key;

    @Parameter(required = false)
    public String outputKey;

    @Parameter(required = false)
    public String visualizeOutputKey;

    private int minBelow = 0;
    private int minAbove = 0;

    private double threshold = 5;

    /**
     * minBelow is minimum number of slices required to be below the threshold before the threshold crossing
     * minAbove is minimum number of slices required to be above the threshold after the threshold crossing
     * threshold is the cutoff value
     *
     */
    @Override
    public Data process(Data item) {
        double[] data = (double[]) item.get(key);
        int roi = data.length / Constants.N_PIXELS;

        //the graph of the array positions visually shows the positions of the crossings for an individual pixel
        double[] positions = new double[data.length];

        //the array CrossPositions contains lists of positions of crossings for each pixel
        int[][] CrossPositions = new int[Constants.N_PIXELS][];

        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
            //creates a list of positions of threshold crossings for a single pixel
            ArrayList<Integer> slices = new ArrayList<Integer>();

            for (int slice = 0; slice < roi; slice++) {
                if (candidate(pix, slice, roi, data) == true) {
                    slices.add(slice);
                    positions[pix * roi + slice] = 5;
                    slice += minAbove;
                } else {
                    positions[pix * roi + slice] = 0;
                }
            }

            //  if no crossing, list for that pixel is empty
            CrossPositions[pix] = new int[slices.size()];
            CrossPositions[pix] = Utils.arrayListToInt(slices);
        }

        item.put(visualizeOutputKey, positions);
        item.put(outputKey, CrossPositions);
        return item;

    }

    //to determine threshold crossings
    public boolean candidate(int pix, int slice, int roi, double[] data) {
        boolean answer = true;

        int pos = pix * roi + slice;

        if (data[pos] < threshold) {
            return false;
        }

        for (int i = 1; i < minBelow; i++) {
            if (slice - i < 0) {
                return false;
            }
            if (data[pos - i] >= threshold) {
                return false;
            }
        }

        for (int k = 0; k < minAbove; k++) {
            if (slice + k > roi) {
                return false;
            }
            if (pos + k == Constants.N_PIXELS * roi) {
                return false;
            }
            if (data[pos + k] <= threshold) {
                return false;
            }
        }

        return answer;
    }
}
