/**
 *
 */
package fact.extraction;

import fact.Constants;
import fact.Utils;
import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * @author Fabian Temme &lt;fabian.temme@tu-dortmund.de&gt;
 */
public class RisingEdgeForPositions implements Processor {

    static Logger log = LoggerFactory.getLogger(RisingEdgeForPositions.class);

    @Parameter(required = true)
    public String dataKey = null;

    @Parameter(required = true)
    public String outputKey = null;

    @Parameter(required = true)
    public String amplitudePositionsKey = null;

    @Parameter(required = true)
    public String maxSlopesKey = null;

    private int searchWindowLeft = 25;

    @Override
    public Data process(Data item) {
        Utils.mapContainsKeys(item, dataKey, amplitudePositionsKey);

        double[] positions = new double[Constants.N_PIXELS];
        double[] maxSlopes = new double[Constants.N_PIXELS];

        double[] data = (double[]) item.get(dataKey);
        int[] amplitudePositions = (int[]) item.get(amplitudePositionsKey);

        IntervalMarker[] m = new IntervalMarker[Constants.N_PIXELS];

        int roi = data.length / Constants.N_PIXELS;

        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
            int posMaxAmp = amplitudePositions[pix];

            // temp. Variables
            double current_slope = 0;
            double max_slope = -Double.MAX_VALUE;
            int search_window_left = posMaxAmp - searchWindowLeft;
            if (search_window_left < 10) {
                search_window_left = 10;
            }
            int search_window_right = posMaxAmp;
            int arrivalPos = search_window_left;
            // Loop over all timeslices of given window
            // check for the largest derivation over 5 slices
            for (int slice = search_window_left; slice < search_window_right; slice++) {
                int pos = pix * roi + slice;
                if (slice + 2 < roi) {
                    current_slope = data[pos + 2] - data[pos - 2];
                } else {
                    break;
                }
                if (current_slope > max_slope) {
                    max_slope = current_slope;
                    arrivalPos = slice;
                }
            }
            positions[pix] = (double) arrivalPos;
            m[pix] = new IntervalMarker(positions[pix], positions[pix] + 1);
            maxSlopes[pix] = (double) max_slope;
        }
        item.put(outputKey, positions);
        item.put(maxSlopesKey, maxSlopes);
        item.put(outputKey + "Marker", m);

        return item;

    }
}
