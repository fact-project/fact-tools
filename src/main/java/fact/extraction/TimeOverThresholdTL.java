/**
 *
 */
package fact.extraction;

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

    public Data process(Data input) {
        Utils.isKeyValid(input, dataKey, double[].class);
        Utils.isKeyValid(input, "NPIX", Integer.class);
        int npix = (Integer) input.get("NPIX");

        int[] timeOverThresholdArray = new int[npix];

        double[] data = (double[]) input.get(dataKey);

        PixelSet pixelSet = new PixelSet();

        int roi = data.length / npix;

        if (range < 0) {
            range = roi - firstSlice;
        }

        //Loop over pixels
        for (int pix = 0; pix < npix; pix++) {

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
        input.put(outputKey, timeOverThresholdArray);
        input.put(outputKey + "SetOverlay", pixelSet);

        //Add totPixelSet only to data item if it is not empty
        if (pixelSet.toIntArray().length != 0) {
            input.put(outputKey + "Set", pixelSet.toIntArray());
        }
        return input;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }


}
