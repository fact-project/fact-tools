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
 * Calculate moving minimum with given window size.
 * <p>
 * Similar to a moving average, this processor calculates
 * a moving minimum. When moving the window over the
 * time series, the processor calculates the minimum inside the
 * window for every time and returns it.
 * <p>
 * This can be used, when answering the question:
 * What is the highest signal with a given width.
 * In conjuction with extraction.MaxAmplitude,
 * the position and the height of the highest signal
 * with a given width (i.e. the window size)
 * can be found.
 *
 * @author Dominik Neise &lt;neised@phys.ethz.ch&gt;
 */
@Description(group = "Fact Tools.Filter", text = "Moving Minimum")
public class MovingMinimum implements StatefulProcessor {

    static Logger log = LoggerFactory.getLogger(MovingMinimum.class);


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

                //loop over the window
                //intentional precission loss by division
                int start = pivotPosition - length / 2;
                int end = pivotPosition + length / 2 + 1;

                start = start > seriesStart ? start : seriesStart;
                end = end < seriesEnd ? end : seriesEnd;

                double min = 1.0e308;
                for (int i = start; i < end; i++) {
                    if (data[i] < min) {
                        min = data[i];
                    }
                }
                result[pivotPosition] = min;
            }
        }
        item.put(outputKey, result);
        return item;
    }

    @Override
    public void init(ProcessContext context) throws Exception {
        if ((length & 1) == 0) {
            length++;
            log.warn("MovingMinimum only supports uneven window lengths. New length is: " + length);
        }
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }
}
