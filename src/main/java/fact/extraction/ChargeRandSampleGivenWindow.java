package fact.extraction;

import fact.Constants;
import fact.Utils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.io.SourceURL;

import java.util.ArrayList;
import java.util.Random;

/**
 * This processor calculates integrals of a given integration window beginning at an random start sample. The next
 * integration window begins with the last sample of the prior window. For a ROI300 event this leads to a sample of
 * up to 10 integrals per event and pixel. The computed integrals can be used to investigate e.g. the NSB dependency of
 * the resulting distribution.
 * Created by jbuss on 17.11.14.
 */
public class ChargeRandSampleGivenWindow implements Processor {
    @Parameter(required = true)
    private String dataKey = null;

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


    @Override
    public Data process(Data input) {

        Utils.mapContainsKeys(input, dataKey, outputKey);

        double[] data;
        data = (double[]) input.get(dataKey);

        double[] chargeMean = new double[Constants.NUMBEROFPIXEL];
        double[] chargeRms  = new double[Constants.NUMBEROFPIXEL];

        int roi = data.length / Constants.NUMBEROFPIXEL;

        Random rand = new Random(Seed);

        int bound = roi - skipLast - skipFirst;
        int iterations = bound/windowSize;

        int[][] sum = new int[Constants.NUMBEROFPIXEL][iterations];

        for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++) {

            int firstSlice = skipFirst + rand.nextInt(bound);
            int startSlice = firstSlice;


            for (int i = 0; i < iterations; i++ ){

                for (int sl = startSlice ; sl > startSlice + windowSize; sl++) {
                    int pos = pix*roi;

                    if (sl < roi - skipLast){
                        pos += sl;
                    }
                    else{
                        pos += skipFirst + sl - (roi - skipLast);
                    }

                    sum[pix][i] += data[pos];
                }
                startSlice += windowSize;
            }

            DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(Utils.intToDoubleArray(sum[pix]));

            chargeMean[pix]  = descriptiveStatistics.getMean();
            chargeRms[pix]   = descriptiveStatistics.getStandardDeviation();
        }

        input.put(outputKey, sum);
        input.put(outputKey+"_mean", chargeMean);
        input.put(outputKey+"_rms", chargeRms);

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

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public long getSeed() {
        return Seed;
    }

    public void setSeed(long seed) {
        Seed = seed;
    }
}
