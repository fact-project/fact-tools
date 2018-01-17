package fact.filter;

import fact.Constants;
import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Description;
import stream.annotations.Parameter;

/**
 * This is processor applies a central moving average to the time series in the camera raw data (NPIX*ROI).
 * This is essentially a smoothing operation. It takes the mean value within a given window of the timeseries and applies that
 * value to the pivot element.
 * <p>
 * Central means it has one pivot element in the center of the window and the same number of elements left and right to it.
 * This implies that only uneven window lengths are supported.
 * <p>
 * This is a somewhat naive implementation which could be made much faster. See the Wikipedia article at
 * https://en.wikipedia.org/wiki/Moving_average
 *
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
@Description(group = "Fact Tools.Filter", text = "A simple running average")
public class MovingAverage implements StatefulProcessor {

    static Logger log = LoggerFactory.getLogger(MovingAverage.class);


    @Parameter(required = true)
    public String key;

    @Parameter(required = true)
    public String outputKey;

    @Parameter(required = true)
    public int length = 5;

    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, key, double[].class);

        double[] data = (double[]) item.get(key);
        double[] result = new double[data.length];

        int roi = data.length / Constants.N_PIXELS;

        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
            for (int pivot = 0; pivot < roi; pivot++) {
                int pivotPosition = pix * roi + pivot;
                int seriesEnd = pix * roi + roi;
                int seriesStart = pix * roi;
                int count = 0;
                //loop over the window
                //intentional precission loss by division
                int start = pivotPosition - length / 2;
                int end = pivotPosition + length / 2 + 1;

                start = start > seriesStart ? start : seriesStart;
                end = end < seriesEnd ? end : seriesEnd;

                double sum = 0;
                for (int i = start; i < end; i++) {
//                    int pos = pix*roi + i;
                    if (i != pivotPosition) {
                        sum += data[i];
                        count++;
                    }
                }
                result[pivotPosition] = sum / count;
            }
        }
        item.put(outputKey, result);
        return item;
    }

    @Override
    public void init(ProcessContext context) throws Exception {
        if ((length & 1) == 0) {
            length++;
            log.warn("CentralMovingAverage only supports uneven window lengths. New length is: " + length);
        }
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }
}
