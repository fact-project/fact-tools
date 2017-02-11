package fact.features;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Created by florian on 25.01.17.
 */
public class TimeSeriesNoise implements Processor {

    @Parameter(required = true, description = "Input key, should have length npix * roi")
    String dataKey = null;

    @Parameter(required = true, description = "Output key")
    String outputKey = null;

    @Parameter(required = false, description = "Start Slice", defaultValue = "10")
    int startSlice = 10;

    @Parameter(required = false, description = "End Slice", defaultValue = "250")
    int endSlice = 250;

    @Override
    public Data process(Data item) {

        double[] data = (double[]) item.get(dataKey);
        int npix = (int) item.get("NPIX");
        int roi = (int) item.get("NROI");

        double[]  stds = new double[npix];
        for (int pix = 0; pix < npix; pix++) {
            DescriptiveStatistics stats = new DescriptiveStatistics();

            for (int slice = this.startSlice; slice < this.endSlice; slice++) {
                stats.addValue(data[pix * roi + slice]);
            }
            stds[pix] = stats.getStandardDeviation();
        }
        item.put(outputKey, stds);
        return item;
    }


    public void setDataKey(String dataKey_) {
        this.dataKey = dataKey_;
    }
    public void setOutputKey(String outputKey_) {
        this.outputKey = outputKey_;
    }
    public void setStartSlice(int startSlice_) {
        this.startSlice = startSlice_;
    }
    public void setEndSlice(int endSlice_) {this.endSlice = endSlice_;
    }
}
