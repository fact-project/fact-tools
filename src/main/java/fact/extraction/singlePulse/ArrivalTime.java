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
 * Finds pulse arrival time by searching the 25 slices prior to the maximum and taking the time slice where the amplitude is equal to or just larger than 1/2 the max.
 * * Input and output are both arrays of size N_PIXELS with lists of positions for each pixel.
 *
 * @author Katie Gray &lt;kathryn.gray@tu-dortmund.de&gt;
 */
public class ArrivalTime implements Processor {
    private static final Logger log = LoggerFactory.getLogger(ArrivalTime.class);

    @Parameter(required = true)
    public String key;

    @Parameter(required = true)
    public String outputKey;

    @Parameter(required = true, description = "Key to the positions of max pulse amplitude")
    public String maxAmpPositionKey;

    @Parameter(required = false)
    public String visualizeKey;

    @Override
    public Data process(Data item) {
        double[] data = (double[]) item.get(key);
        int[][] maxAmpPositions = (int[][]) item.get(maxAmpPositionKey);
        int roi = data.length / Constants.N_PIXELS;
        int[][] arrivalTimes = new int[Constants.N_PIXELS][];
        double[] visualizePositions = new double[data.length];
        //zero for all positions except where an arrival time is found

        for (int i = 0; i < data.length; i++) {
            visualizePositions[i] = 0;
        }

        //for each pixel
        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
            arrivalTimes[pix] = new int[maxAmpPositions.length];
            arrivalTimes[pix] = findArrivalTimes(pix, roi, data, maxAmpPositions, visualizePositions);
        }
        item.put(outputKey, arrivalTimes);
        item.put(visualizeKey, visualizePositions);
        //     System.out.println(Arrays.toString(arrivalTimes));


        return item;
    }

    /**
     * @param pix  Pixel to check
     * @param roi  Basically the number of slices in one event
     * @param data the array which to check
     * @return
     */

    public int[] findArrivalTimes(int pix, int roi, double[] data, int[][] maxAmpPositions, double[] visualizePositions) {

        ArrayList<Integer> positions = new ArrayList<Integer>();

        if (maxAmpPositions[pix].length > 0) {
            int numberPulses = maxAmpPositions[pix].length;
            for (int i = 0; i < numberPulses; i++) {
                int Position = 0;
                int end = maxAmpPositions[pix][i];
                int endPos = pix * roi + end;
                for (int slice = end; slice > end - 25; slice--) {
                    int pos = pix * roi + slice;
                    if (end - 25 < 0) {
                        continue;
                    }
                    double value = data[pos];
                    if (slice > 0 && slice + 80 < roi && end - slice < 15) {
                        if (value <= data[endPos] / 2) {
                            Position = slice;
                            break;
                        }
                    }
                }
                if (Position != 0) {
                    positions.add(Position);
                    visualizePositions[pix * roi + Position] = 15;
                }
            }
        }

        return Utils.arrayListToInt(positions);
    }
}
