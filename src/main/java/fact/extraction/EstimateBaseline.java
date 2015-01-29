package fact.extraction;

import fact.Constants;
import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/** Simple processor tho estimate the baseline from the average of the slices of a given window
 * Created by jbuss on 29.01.15.
 */
public class EstimateBaseline implements Processor {
    @Parameter(required=true, description="")
    private String dataKey = null;

    @Parameter(required=true)
    private String outputKey = null;

    @Parameter(required = false, description="start slice of the calculation window", defaultValue="10")
    protected int firstSlice = 10;
    @Parameter(required = false, description="range of the calculation window ", defaultValue="40")
    protected int range = 40;

    private int npix = Constants.NUMBEROFPIXEL;
    private int roi  = 300;
    @Override
    public Data process(Data input) {

        Utils.isKeyValid(input, "NPIX", Integer.class);
        Utils.mapContainsKeys(input, dataKey,  "NPIX");

        npix        = (Integer) input.get("NPIX");
        roi         = (Integer) input.get("NROI");

        double[] data       = (double[]) input.get(dataKey);
        double[] baseline  = new double[npix];

        for (int pix = 0; pix < npix; pix++){
            int firstSl     = pix*roi + firstSlice;
            int lastSl      = firstSl + range;

            if (lastSl > roi*(pix+1)){

                //put here a runtime exeption
            }

            for (int slice = firstSl; slice < lastSl; slice++){
                baseline[pix] += data[slice];
            }
            baseline[pix] /= range;
        }
        input.put(outputKey, baseline);
        return input;
    }

    public String getDataKey() {
        return dataKey;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public int getFirstSlice() {
        return firstSlice;
    }

    public void setFirstSlice(int firstSlice) {
        this.firstSlice = firstSlice;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }
}
