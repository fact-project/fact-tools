package fact.extraction;

import fact.Constants;
import fact.Utils;
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

    private int npix = Constants.NUMBEROFPIXEL;
    private int roi = 300;

    @Override
    public Data process(Data input) {

        Utils.isKeyValid(input, "NPIX", Integer.class);
        Utils.mapContainsKeys(input, dataKey, "NPIX");

        npix = (Integer) input.get("NPIX");
        roi = (Integer) input.get("NROI");

        double[] data = (double[]) input.get(dataKey);
        double[] baseline = new double[npix];

        double[] mBslLevel = new double[data.length];

        IntervalMarker[] mBslRange = new IntervalMarker[npix];

        for (int pix = 0; pix < npix; pix++) {
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
        input.put(outputKey, baseline);
        input.put(outputKey + "_range", mBslRange);
        input.put(outputKey + "_level", mBslLevel);
        return input;
    }
}
