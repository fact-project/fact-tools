package fact.extraction;

import fact.Constants;
import org.jfree.chart.plot.IntervalMarker;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Simple processor tho estimate the baseline amplitude with the average from the slices of a given window
 * Created by jbuss on 29.01.15.
 */
public class EstimateBaseline implements Processor {
    @Parameter(required = true, description = "")
    public String dataKey = null;

    @Parameter(required = true, description = "key for the baseline output, 1440 pixel array containing a baseline amplitude for each pixel")
    public String outputKey = null;

    @Parameter(required = false, description = "start slice of the calculation window", defaultValue = "10")
    public int firstSlice = 10;

    @Parameter(required = false, description = "range of the calculation window ", defaultValue = "40")
    public int range = 40;

    private int roi = 300;

    @Override
    public Data process(Data item) {

        roi = (Integer) item.get("NROI");

        double[] data = (double[]) item.get(dataKey);
        double[] baseline = new double[Constants.N_PIXELS];

        double[] mBslLevel = new double[data.length];

        IntervalMarker[] mBslRange = new IntervalMarker[Constants.N_PIXELS];

        for (int pix = 0; pix < Constants.N_PIXELS; pix++) {
            int firstSl = pix * roi + firstSlice;
            int lastSl = firstSl + range;

            if (lastSl > roi * (pix + 1)) {

                //put here a runtime exeption
            }

            for (int slice = firstSl; slice < lastSl; slice++) {
                baseline[pix] += data[slice];
            }
            baseline[pix] /= range;
            mBslRange[pix] = new IntervalMarker(firstSlice, firstSlice + range);
            for (int slice = firstSl; slice < lastSl; slice++) {
                mBslLevel[slice] = baseline[pix];
            }

        }
        item.put(outputKey, baseline);
        item.put(outputKey + "_range", mBslRange);
        item.put(outputKey + "_level", mBslLevel);
        return item;
    }
}
