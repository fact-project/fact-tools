/**
 *
 */
package fact.extraction;

import fact.Constants;
import fact.Utils;
import fact.container.PixelSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This feature is supposed to give the number of slices above a given Threshold,
 * caclulated for a defined extraction window
 *
 * @author <a href="mailto:jens.buss@tu-dortmund.de">Jens Buss</a>
 */
public class TimeOverThresholdTL implements Processor {
    static Logger log = LoggerFactory.getLogger(TimeOverThresholdTL.class);

    @Parameter(required = true)
    public String dataKey = null;

    @Parameter(required = false)
    public double threshold = 50;

    @Parameter
    public int range = -1;

    @Parameter
    public int firstSlice = 0;

    @Parameter(required = true)
    public String outputKey = null;

    public Data process(Data item) {
        Utils.isKeyValid(item, dataKey, double[].class);

        int[] timeOverThresholdArray = new int[Constants.N_PIXELS];

        double[] data = (double[]) item.get(dataKey);

        PixelSet pixelSet = new PixelSet();

        int roi = data.length / Constants.N_PIXELS;

        if (range < 0) {
            range = roi - firstSlice;
        }

        //Loop over pixels
        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {

            int pos = pix * roi;

            int timeOverThreshold = 0;

            //Loop over slices
            for (int sl = firstSlice; sl < range; sl++) {
                if (data[pos + sl] > threshold) {
                    timeOverThreshold++;
                }
            }

            if (timeOverThreshold > 0) {
                pixelSet.addById(pix);
            }

            timeOverThresholdArray[pix] = timeOverThreshold;
        }


        //add times over threshold to the DataItem
        item.put(outputKey, timeOverThresholdArray);
        item.put(outputKey + "SetOverlay", pixelSet);

        //Add totPixelSet only to data item if it is not empty
        if (pixelSet.toIntArray().length != 0) {
            item.put(outputKey + "Set", pixelSet.toIntArray());
        }
        return item;
    }
}
