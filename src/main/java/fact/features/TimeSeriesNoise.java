package fact.features;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Created by florian on 25.01.17.
 */

// does nearly the same as implemented in fact.features.ArrayMean, just with array limits
public class TimeSeriesNoise implements Processor {

    @Parameter(required = true, description = "Input key, should have length NPIC * NROI")
    String dataKey = null;

    @Parameter(required = true, description = "Output key for the mean array of the selected slice range")
    String outputKeyMean = null;

    @Parameter(required = true, description = "Output key for the standard deviation array of the selected slice range")
    String outputKeyStd = null;

    @Parameter(required = false, description = "Start Slice", defaultValue = "10")
    int startSlice = 10;

    @Parameter(required = false, description = "End Slice", defaultValue = "250")
    int endSlice = 250;

    @Override
    public Data process(Data item) {

        double[] data = (double[]) item.get(dataKey);
        int npix = (int) item.get("NPIX");
        int roi = (int) item.get("NROI");

        float[]  mean = new float[npix];
        float[]  stds = new float[npix];
        for (int pix = 0; pix < npix; pix++) {
            DescriptiveStatistics stats = new DescriptiveStatistics();

            for (int slice = this.startSlice; slice < this.endSlice; slice++) {
                stats.addValue(data[pix * roi + slice]);
            }
            mean[pix] = (float) stats.getMean();
            stds[pix] = (float) stats.getStandardDeviation();
        }
        item.put(outputKeyMean, mean);
        item.put(outputKeyStd, stds);

        return item;
    }


    public void setDataKey(String dataKey_) {
        this.dataKey = dataKey_;
    }
    public void setOutputKeyMean(String outputKeyMean_) {
        this.outputKeyMean = outputKeyMean_;
    }
    public void setOutputKey(String outputKeyStd_) {
        this.outputKeyStd = outputKeyStd_;
    }
    public void setStartSlice(int startSlice_) {
        this.startSlice = startSlice_;
    }
    public void setEndSlice(int endSlice_) {this.endSlice = endSlice_;
    }
}
