package fact.extraction;

import fact.Constants;
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
    public String key = null;

    @Parameter(required = true)
    public String outputKey = null;

    @Parameter(description = "Key of the pixel sample that should be used", defaultValue = "")
    public String pixelSetKey = null;

    @Parameter(description = "Number of slices to be skipped at the time lines beginning", defaultValue = "50")
    public int skipFirst = 35;

    @Parameter(description = "Number of slices to be skipped at the time lines end", defaultValue = "50")
    public int skipLast = 100;

    @Parameter(description = "Size of the integration window", defaultValue = "30")
    public int windowSize = 30;

    @Parameter(description = "Seed of the random number generator")
    public long Seed = 5901;

    private static final Logger log = LoggerFactory.getLogger(WaveformFluctuation.class);

    @Override
    public Data process(Data item) {

        Utils.mapContainsKeys(item, key);
        int[] pixels = Utils.getValidPixelSetAsIntArr(item, Constants.N_PIXELS, pixelSetKey);
        log.debug("npix: " + pixels.length);

        double[] data = (double[]) item.get(key);

        double[] chargeMean = new double[Constants.N_PIXELS];
        double[] chargeStd = new double[Constants.N_PIXELS];
        double[] chargeVariance = new double[Constants.N_PIXELS];
        double[] chargeKurtosis = new double[Constants.N_PIXELS];
        double[] chargeMax = new double[Constants.N_PIXELS];
        double[] chargeMin = new double[Constants.N_PIXELS];
        double[] chargeSkewness = new double[Constants.N_PIXELS];
        double[] chargeMedian = new double[Constants.N_PIXELS];
        double[] chargeSum = new double[Constants.N_PIXELS];

        int roi = data.length / Constants.N_PIXELS;

        Random rand = new Random(Seed);

        int bound = roi - skipLast - skipFirst;
        int iterations = bound / windowSize;
        log.debug("Iterations: " + iterations);

        double[][] charge = new double[Constants.N_PIXELS][iterations];


        //Loop over all pixel and calculate integrals on timeline
        for (int pix : pixels) {
            int startSlice = skipFirst + rand.nextInt(bound);

            double[] integral = new double[iterations];

            //loop over windows on time line
            for (int i = 0; i < iterations; i++) {

                integral[i] = 0.;

                //loop over slices for a given window and integrate slice's amplitudes
                for (int sl = startSlice; sl < startSlice + windowSize; sl++) {
                    int pos = pix * roi;

                    // Check if current slices is within the time line's boundaries
                    if (sl < roi - skipLast) {
                        pos += sl;
                    } else {
                        //start at the time lines beginning if slice is beyond boundaries
                        pos += skipFirst + sl - (roi - skipLast);
                    }
                    integral[i] += data[pos];

                }
                startSlice += windowSize;
            }
            charge[pix] = integral;

            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(integral);
            chargeMean[pix] = descriptiveStatistics.getMean();
            chargeStd[pix] = descriptiveStatistics.getStandardDeviation();
            chargeVariance[pix] = descriptiveStatistics.getVariance();
            chargeKurtosis[pix] = descriptiveStatistics.getKurtosis();
            chargeMax[pix] = descriptiveStatistics.getMax();
            chargeMin[pix] = descriptiveStatistics.getMin();
            chargeSkewness[pix] = descriptiveStatistics.getSkewness();
            chargeMedian[pix] = descriptiveStatistics.getPercentile(0.5);
            chargeSum[pix] = descriptiveStatistics.getSum();


        }

        item.put(outputKey, charge);
        item.put(outputKey + "_mean", chargeMean);
        item.put(outputKey + "_std", chargeStd);
        item.put(outputKey + "_var", chargeVariance);
        item.put(outputKey + "_kurtosis", chargeKurtosis);
        item.put(outputKey + "_max", chargeMax);
        item.put(outputKey + "_min", chargeMin);
        item.put(outputKey + "_skewness", chargeSkewness);
        item.put(outputKey + "_median", chargeMedian);
        item.put(outputKey + "_sum", chargeSum);

        return item;
    }
}
