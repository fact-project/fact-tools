package fact.extraction;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import fact.Utils;
import fact.container.PixelSet;
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
public class TimeSeriesFluctuation implements Processor {
    @Parameter(required = false, defaultValue = "raw:dataCalibrated")
    private String dataKey = "raw:dataCalibrated";

    @Parameter(required = false, defaultValue = "pixels:pedvar")
    private String outputKey = "pixels:pedvar";

    @Parameter(required = false, description = "Number of slices to be skipped at the time series' beginning", defaultValue = "35")
    private int skipFirst = 35;

    @Parameter(required = false, description = "Number of slices to be skipped at the time series' end", defaultValue = "100")
    private int skipLast = 100;

    @Parameter(required = false, description = "Size of the integration window", defaultValue = "30")
    private int windowSize = 30;

    @Parameter(required = false, description = "Seed of the random number generator", defaultValue="5901")
    private long Seed = 5901;

    // A logger
    static Logger log = LoggerFactory.getLogger(TimeSeriesFluctuation.class);

    private int npix;

    @Override
    public Data process(Data item) {

        Utils.mapContainsKeys(item, dataKey);
        Utils.isKeyValid(item, "NPIX", Integer.class);
        npix = (Integer) item.get("NPIX");

        ContiguousSet<Integer> numbers = ContiguousSet.create(Range.closed(0, npix-1), DiscreteDomain.integers());
        int[] pixels = Ints.toArray(numbers);

        double[] data        = (double[]) item.get(dataKey);

        double[] chargeMean             = new double[npix];
        double[] chargeStd              = new double[npix];
        double[] chargeVariance         = new double[npix];
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

        //Loop over all pixel and calculate integrals on timeseries
        for (int pix : pixels) {
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
            chargeVariance[pix]     = descriptiveStatistics.getVariance();
            chargeKurtosis[pix]     = descriptiveStatistics.getKurtosis();
            chargeMax[pix]          = descriptiveStatistics.getMax();
            chargeMin[pix]          = descriptiveStatistics.getMin();
            chargeSkewness[pix]     = descriptiveStatistics.getSkewness();
            chargeMedian[pix]       = descriptiveStatistics.getPercentile(0.5);
            chargeSum[pix]          = descriptiveStatistics.getSum();


        }

        item.put(outputKey, charge);
        item.put(outputKey+":mean", chargeMean);
        item.put(outputKey+":std", chargeStd);
        item.put(outputKey+":var", chargeVariance);
        item.put(outputKey+":kurtosis",chargeKurtosis);
        item.put(outputKey+":max",chargeMax);
        item.put(outputKey+":min",chargeMin);
        item.put(outputKey+":skewness",chargeSkewness);
        item.put(outputKey+":median",chargeMedian);
        item.put(outputKey+":sum",chargeSum);

        return item;
    }
}
