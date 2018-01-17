/**
 *
 */
package fact.extraction;

import fact.Constants;
import fact.Utils;
import fact.container.PixelSet;
import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This feature is supposed to give the number of slices above a given Threshold,
 * in order to calculate the width of a Signal
 *
 * @author <a href="mailto:jens.buss@tu-dortmund.de">Jens Buss</a>
 */
public class TimeOverThreshold implements Processor {
    static Logger log = LoggerFactory.getLogger(TimeOverThreshold.class);

    @Parameter(required = true)
    public String dataKey = null;

    @Parameter(required = true)
    public String positionsKey = null;

    @Parameter(required = true)
    public double threshold = 50;

    @Parameter(required = true)
    public String thresholdOutputKey = null;

    @Parameter(required = true)
    public String firstSliceOverThresholdOutputKey = null;

    @Parameter(required = true)
    public String outputKey = null;

    public Data process(Data item) {
        Utils.isKeyValid(item, dataKey, double[].class);
        Utils.isKeyValid(item, positionsKey, int[].class);

        int[] timeOverThresholdArray = new int[Constants.N_PIXELS];
        double[] firstSliceOverThresholdArray = new double[Constants.N_PIXELS];

        double[] data = (double[]) item.get(dataKey);
        int[] posArray = (int[]) item.get(positionsKey);

        IntervalMarker[] m = new IntervalMarker[Constants.N_PIXELS];

        int roi = data.length / Constants.N_PIXELS;
        int numPixelAboveThreshold = 0;

        PixelSet pixelSet = new PixelSet();

        //Loop over pixels
        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
            firstSliceOverThresholdArray[pix] = 0;

            int pos = pix * roi;
            int positionOfMaximum = posArray[pix];

            //Check if maximum is above threshold otherwise skip the pixel
            if (data[pos + positionOfMaximum] < threshold) {
                continue;
            }
            pixelSet.addById(pix);
            numPixelAboveThreshold++;

            int timeOverThreshold = 0;
            int firstSliceOverThresh = 0;
            int lastSliceOverThresh = 0;

            //Loop over slices before Maximum and sum up those above threshold
            for (int sl = positionOfMaximum;
                 sl > 0; sl--) {
                if (data[pos + sl] < threshold) {
                    firstSliceOverThresh = sl + 1;
                    break;
                }

                timeOverThreshold++;
            }

            //Loop over slices after Maximum and sum up those above threshold
            for (int sl = positionOfMaximum + 1;
                 sl < roi; sl++) {
                if (data[pos + sl] < threshold) {
                    lastSliceOverThresh = sl - 1;
                    break;
                }

                timeOverThreshold++;
            }


            timeOverThresholdArray[pix] = timeOverThreshold;
            firstSliceOverThresholdArray[pix] = (double) firstSliceOverThresh;
            m[pix] = new IntervalMarker(firstSliceOverThresh, lastSliceOverThresh);
        }


        //add processors threshold to the DataItem
        item.put(thresholdOutputKey, threshold);

        //add number of pixel above this threshold to the DataItem
        item.put(outputKey + "_numPixel", numPixelAboveThreshold);

        //add times over threshold to the DataItem
        item.put(outputKey, timeOverThresholdArray);
        item.put(firstSliceOverThresholdOutputKey, firstSliceOverThresholdArray);
        item.put(outputKey + "Marker", m);
        item.put(outputKey + "SetOverlay", pixelSet);


        item.put(outputKey + "Set", pixelSet.toIntArray());
        return item;
    }
}
