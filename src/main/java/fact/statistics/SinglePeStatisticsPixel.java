package fact.statistics;

import fact.Utils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;


/**
 * Calculate the Statistics of Pixel Arrays
 *
 * @author Jens Buss
 */
public class SinglePeStatisticsPixel implements Processor {

    @Parameter(required = true, description = "key to the data array")
    public String dataKey = null;

    @Parameter(required = true, description = "name of the key of the calculated features")
    public String outputKey = null;

    private int npix;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");

        Utils.mapContainsKeys(input, dataKey);

        int[][] data = (int[][]) input.get(dataKey);

        double[] mean = new double[npix];
        double[] median = new double[npix];
        double[] mode = new double[npix];
        double[] std = new double[npix];
        double[] kurtosis = new double[npix];
        double[] skewness = new double[npix];
        double[] min = new double[npix];
        double[] max = new double[npix];
        double[] quantil25 = new double[npix];
        double[] quantil75 = new double[npix];

        for (int pix = 0; pix < npix; pix++) {
            double[] values = Utils.toDoubleArray(data[pix]);


            ///FIXME: fill nans instead of continue
            if (values.length == 0) {
                continue;
            }

            DescriptiveStatistics stats = new DescriptiveStatistics(values);
            mean[pix] = stats.getMean();
            min[pix] = stats.getMin();
            max[pix] = stats.getMax();
            std[pix] = stats.getStandardDeviation();
            skewness[pix] = stats.getSkewness();
            kurtosis[pix] = stats.getKurtosis();
            quantil25[pix] = stats.getPercentile(0.25);
            quantil75[pix] = stats.getPercentile(0.75);
            median[pix] = stats.getPercentile(0.5);

            double[] modeArray = StatUtils.mode(values);
            mode[pix] = modeArray[0];
        }

        input.put(outputKey + "_mean", mean);
        input.put(outputKey + "_median", median);
        input.put(outputKey + "_mode", mode);
        input.put(outputKey + "_std", std);
        input.put(outputKey + "_kurtosis", kurtosis);
        input.put(outputKey + "_skewness", skewness);
        input.put(outputKey + "_min", min);
        input.put(outputKey + "_max", max);
        input.put(outputKey + "_quantil25", quantil25);
        input.put(outputKey + "_quantil75", quantil75);


        return input;
    }
}
