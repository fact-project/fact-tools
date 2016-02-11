package fact.extraction;

import fact.Constants;
import fact.Utils;
import org.jfree.chart.plot.IntervalMarker;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/** Simple processor tho estimate the baseline amplitude with the average from the slices of a given window
 * Created by jbuss on 29.01.15.
 */
public class EstimateBaseline implements Processor {
    @Parameter(required=false, description="key for the data object on which the baseline should estimated.", defaultValue="raw:dataCalibrated")
    private String dataKey = "raw:dataCalibrated";

    @Parameter(required=false, description="key for the baseline output, 1440 pixel array containing a baseline amplitude for each pixel", defaultValue="pixels:baselines")
    private String outputKey = "pixels:baselines";

    @Parameter(required = false, description="start slice of the calculation window", defaultValue="10")
    protected int firstSlice = 10;
    @Parameter(required = false, description="range of the calculation window ", defaultValue="30")
    protected int range = 30;

    private int npix = Constants.NUMBEROFPIXEL;
    private int roi  = 300;
    @Override
    public Data process(Data item) {

        Utils.isKeyValid(item, "NPIX", Integer.class);
        Utils.mapContainsKeys(item, dataKey,  "NPIX");

        npix        = (Integer) item.get("NPIX");
        roi         = (Integer) item.get("NROI");

        double[] data       = (double[]) item.get(dataKey);
        double[] baselines  = new double[npix];

        double[] mBslLevel  = new double[data.length];

        IntervalMarker[] markerBslRange  = new IntervalMarker[npix];

        for (int pix = 0; pix < npix; pix++){
            int firstSl     = pix*roi + firstSlice;
            int lastSl      = firstSl + range;

            if (lastSl > roi*(pix+1)){

                //put here a runtime exeption
            }

            for (int slice = firstSl; slice < lastSl; slice++){
                baselines[pix] += data[slice];
            }
            baselines[pix] /= range;
            markerBslRange[pix] = new IntervalMarker(firstSlice,firstSlice + range);
            for (int slice = firstSl; slice < lastSl; slice++){
                mBslLevel[slice] = baselines[pix];
            }

        }
        item.put(outputKey, baselines);
        item.put(outputKey+"_range", markerBslRange);
        item.put(outputKey+"_level", mBslLevel);
        return item;
    }
}
