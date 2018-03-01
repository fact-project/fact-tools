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
 * This processor calculates the position of the maximum value for each pulse in each pixel.
 * Input and output are both arrays of size N_PIXELS with lists of positions for each pixel.
 * <p>
 * modified by Katie Gray (kathryn.gray@tu-dortmund.de) from MaxAmplitudePosition
 */
public class PulseMaxAmplitude implements Processor {
    static Logger log = LoggerFactory.getLogger(PulseMaxAmplitude.class);

    @Parameter(required = true)
    public String key;

    @Parameter(required = true)
    public String outputKey;
    //positions of max amplitudes of pulses

    @Parameter(required = true)
    public String pulsePositionKey;
    //positions of threshold crossings

    @Override
    public Data process(Data item) {
        double[] data = (double[]) item.get(key);
        int[][] pulsePositions = (int[][]) item.get(pulsePositionKey);
        int roi = data.length / Constants.N_PIXELS;
        int[][] positions = new int[Constants.N_PIXELS][];

        //for each pixel
        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
            positions[pix] = new int[pulsePositions[pix].length];
            positions[pix] = findMaximumPositions(pix, roi, data, pulsePositions);
        }
        item.put(outputKey, positions);
//      System.out.println(Arrays.toString(positions));

        return item;
    }

    /**
     * finds the position of the highest value in the pulse. if max is not unique, last position will be taken.
     *
     * @param pix  Pixel to check
     * @param roi  Basically the number of slices in one event
     * @param data the array which to check
     * @return
     */

    public int[] findMaximumPositions(int pix, int roi, double[] data, int[][] pulsePositions) {

        ArrayList<Integer> maxima = new ArrayList<Integer>();

        if (pulsePositions[pix].length > 0) {
            int numberPulses = pulsePositions[pix].length;
            for (int i = 0; i < numberPulses; i++) {
                double tempMaxValue = 0;
                int Position = 0;
                int start = pulsePositions[pix][i];
                for (int slice = start; slice < start + 30; slice++) {
                    int pos = pix * roi + slice;
                    if (slice > roi) {
                        break;
                    }
                    if (pos == data.length) {
                        break;
                    }
                    double value = data[pos];
                    //update maxvalue and position if current value exceeds old value
                    if (slice != start && slice != start + 30) {
                        if (value >= tempMaxValue) {
                            tempMaxValue = value;
                            Position = slice;
                        }
                    }
                }
                maxima.add(Position);
            }
        }

        return Utils.arrayListToInt(maxima);
    }
}
