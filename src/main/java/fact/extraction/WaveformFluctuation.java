package fact.extraction;

import fact.Utils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.Random;

/**
 * This processor calculates integrals of a given integration window beginning at an random start sample. The next
 * integration window begins with the last sample of the prior window. For a ROI300 event this leads to a sample of
 * up to 10 integrals per event and pixel. The computed integrals can be used to investigate e.g. the NSB dependency of
 * the resulting distribution.
 * Created by jbuss on 17.11.14.
 */
public class WaveformFluctuation implements Processor {
    @Parameter(required = true)
    private String key = null;

    @Parameter(required = true)
    private String outputKey = null;

    @Parameter(description = "Number of slices to be skipped at the time lines beginning", defaultValue = "50")
    private int skipFirst = 25;

    @Parameter(description = "Number of slices to be skipped at the time lines beginning", defaultValue = "50")
    private int skipLast = 25;

    @Parameter(description = "Size of the integration window", defaultValue = "30")
    private int windowSize = 30;

    @Parameter(description = "Seed of the random number generator")
    private long Seed = 5901;

    // A logger
    static Logger log = LoggerFactory.getLogger(WaveformFluctuation.class);

    private int npix;

    @Override
    public Data process(Data input) {

        Utils.mapContainsKeys(input, key);
        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");

        double[] data        = (double[]) input.get(key);

        double[] chargeMean             = new double[npix];
        double[] chargeStd              = new double[npix];
        double[] chargeKurtosis         = new double[npix];
        double[] chargeMax              = new double[npix];
        double[] chargeMin              = new double[npix];
        double[] chargeSkewness         = new double[npix];
        double[] chargeMedian           = new double[npix];
        double[] chargeSum              = new double[npix];

        int roi = data.length / npix;

        Random rand = new Random(Seed);

        int bound = roi - skipLast - skipFirst;
        int iterations = bound/windowSize;
        log.info("Iterations: " + iterations );

        double[][] charge = new double[npix][iterations];

        //Loop over all pixel and calculate integrals on timeline
        for (int pix = 0; pix < npix; pix++) {
            int startSlice = skipFirst + rand.nextInt(bound);

            double[] integral = new double[iterations];

            //loop over windows on time line
            for (int i = 0; i < iterations; i++ ){

                integral[i] = 0.;

                //loop over slices for a given window and integrate slice's amplitudes
                for (int sl = startSlice ; sl < startSlice + windowSize; sl++) {
                    int pos = pix*roi;

                    // Check if current slices is within the time line's boundaries
                    if (sl < roi - skipLast) {
                        pos += sl;
                    }
                    else{
                        //start at the time lines beginning if slice is beyond boundaries
                        pos += skipFirst + sl - (roi - skipLast);
                    }
                    integral[i] += data[pos];

                }
                startSlice += windowSize;
            }
            charge[pix] = integral;

            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics( integral );

            chargeMean[pix]         = descriptiveStatistics.getMean();
            chargeStd[pix]          = descriptiveStatistics.getStandardDeviation();
            chargeKurtosis[pix]     = descriptiveStatistics.getKurtosis();
            chargeMax[pix]          = descriptiveStatistics.getMax();
            chargeMin[pix]          = descriptiveStatistics.getMin();
            chargeSkewness[pix]     = descriptiveStatistics.getSkewness();
            chargeMedian[pix]       = descriptiveStatistics.getPercentile(0.5);
            chargeSum[pix]          = descriptiveStatistics.getSum();


        }

        input.put(outputKey, charge);
        input.put(outputKey+"_mean", chargeMean);
        input.put(outputKey+"_std", chargeStd);
        input.put(outputKey+"_kurtosis",chargeKurtosis);
        input.put(outputKey+"_max",chargeMax);
        input.put(outputKey+"_min",chargeMin);
        input.put(outputKey+"_skewness",chargeSkewness);
        input.put(outputKey+"_median",chargeMedian);
        input.put(outputKey+"_sum",chargeSum);

        return input;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public int getSkipFirst() {
        return skipFirst;
    }

    public void setSkipFirst(int skipFirst) {
        this.skipFirst = skipFirst;
    }

    public int getSkipLast() {
        return skipLast;
    }

    public void setSkipLast(int skipLast) {
        this.skipLast = skipLast;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public long getSeed() {
        return Seed;
    }

    public void setSeed(long seed) {
        Seed = seed;
    }
}