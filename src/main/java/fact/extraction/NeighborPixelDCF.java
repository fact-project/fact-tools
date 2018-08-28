package fact.extraction;

import fact.Constants;
import fact.Utils;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactPixelMapping;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

import java.util.Arrays;

/**
 * Calculate the Descrete Correlation Function for the Time Series of neigbouring pixels as a measure for their
 * correllation
 * <p>
 * Created by jebuss on 10.08.16.
 */
public class NeighborPixelDCF implements StatefulProcessor {
    @Parameter(required = true, description = "raw data array")
    public String key = null;

    @Parameter(required = false, description = "pixel array containing a noise estimation for each pixel")
    public String noiseKey = null;

    @Parameter(description = "Key of the pixel sample that should be used, " +
            "if no pixelset is given, the whole camera is used", defaultValue = "")
    public String pixelSetKey = null;

    @Parameter(description = "Number of slices to be skipped at the time lines beginning", defaultValue = "30")
    public int skipFirst = 30;

    @Parameter(description = "Number of slices to be skipped at the time lines end", defaultValue = "50")
    public int skipLast = 50;

    @Parameter(required = false, description = "Outputkey for the mean correlation of neighbouring pixels")
    public String neighborPixDCFKey = "neighborPixDCF";

    private int deltaTMax = 5;

    private int roi = 300;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    // A logger
    static Logger log = LoggerFactory.getLogger(NeighborPixelDCF.class);

    double[] default_noise;

    @Override
    public Data process(Data input) {

        double[] data = (double[]) input.get(key);
        double[] noise = default_noise;
        if (noiseKey != null) {
            noise = (double[]) input.get(noiseKey);
        }

        int[] pixels = Utils.getValidPixelSetAsIntArr(input, Constants.N_PIXELS, pixelSetKey);
        log.debug("npix: " + pixels.length);

        roi = data.length / Constants.N_PIXELS;

        //snip pixel Data to arrays without skipped slices
        double[][] snipedPixelData = Utils.snipPixelData(data, skipFirst, skipLast, Constants.N_PIXELS, roi);

        //get mean and variance of the timeseries for each pixel
        DescriptiveStatistics[] pixelStatistics = Utils.calculateTimeseriesStatistics(snipedPixelData);

        double[] meanPixDCF = new double[Constants.N_PIXELS];
        double[] meanPixDCFDeltaT = new double[Constants.N_PIXELS];

        double[] stdDevPixDCF = new double[Constants.N_PIXELS];
        double[] stdDevPixDCFDeltaT = new double[Constants.N_PIXELS];

        double[] maxPixDCF = new double[Constants.N_PIXELS];
        double[] maxPixDCFDeltaT = new double[Constants.N_PIXELS];

        double[] minPixDCF = new double[Constants.N_PIXELS];
        double[] minPixDCFDeltaT = new double[Constants.N_PIXELS];

        // TODO: 11.08.16 Make sure that each pair of pixels is only touched once

        //Loop over all pixels to calculate the mean correlation with their neighbours
        for (int pix : pixels) {
            CameraPixel[] neighbours = pixelMap.getNeighborsFromID(pix);


            double pixStdDev = pixelStatistics[pix].getStandardDeviation();
            double pixMean = pixelStatistics[pix].getMean();

            int numNeighbours = neighbours.length;

            double[] maxNeighborDcf = new double[numNeighbours];
            double[] maxNeighborDcfDeltaT = new double[numNeighbours];

            int counter = 0;
            //Loop over all neighbour pixels to calculate the correlation with the given pixel
            for (CameraPixel neighbour : neighbours) {

                //exclude pixel that are not contained in the pixel set             }
                if (!ArrayUtils.contains(pixels, neighbour.id)) {
                    numNeighbours -= 1;
                    continue;
                }

                double neighbourStdDev = pixelStatistics[neighbour.id].getStandardDeviation();
                double neighbourMean = pixelStatistics[neighbour.id].getMean();

                int[] deltaT = new int[2 * deltaTMax + 1];

                for (int t = 0; t <= deltaTMax; t++) {
                    deltaT[deltaTMax + t] = t;
                    if (t > 0) {
                        deltaT[deltaTMax - t] = -t;
                    }
                }

                double[] dcf = new double[2 * deltaTMax + 1];

                double maxDcf = Double.MIN_VALUE;
                int maxDcfDeltaT = Integer.MAX_VALUE;

                double udcfNorm = UDCFNorm(pixStdDev, neighbourStdDev, noise[pix], noise[neighbour.id]);

                for (int t : deltaT) {
                    dcf[deltaTMax + t] = DCF(t, snipedPixelData[pix], snipedPixelData[neighbour.id],
                            pixMean, neighbourMean, udcfNorm);
                    if (dcf[deltaTMax + t] > maxDcf) {
                        maxDcf = dcf[deltaTMax + t];
                        maxDcfDeltaT = deltaT[deltaTMax + t];
                    }
                }

                maxNeighborDcf[counter] = maxDcf;
                maxNeighborDcfDeltaT[counter] = Math.abs(maxDcfDeltaT);
                counter++;
            }

            DescriptiveStatistics statisticsDCF = new DescriptiveStatistics(maxNeighborDcf);
            DescriptiveStatistics statisticsDCFDeltaT = new DescriptiveStatistics(maxNeighborDcfDeltaT);

            meanPixDCF[pix] = statisticsDCF.getMean();
            meanPixDCFDeltaT[pix] = statisticsDCFDeltaT.getMean();

            stdDevPixDCF[pix] = statisticsDCF.getStandardDeviation();
            stdDevPixDCFDeltaT[pix] = statisticsDCFDeltaT.getStandardDeviation();

            maxPixDCF[pix] = statisticsDCF.getMax();
            maxPixDCFDeltaT[pix] = statisticsDCFDeltaT.getMax();

            minPixDCF[pix] = statisticsDCF.getMin();
            minPixDCFDeltaT[pix] = statisticsDCFDeltaT.getMin();

        }


        input.put(neighborPixDCFKey + "_mean", meanPixDCF);
        input.put(neighborPixDCFKey + "_stdDev", stdDevPixDCF);
        input.put(neighborPixDCFKey + "_max", maxPixDCF);
        input.put(neighborPixDCFKey + "_min", minPixDCF);
        input.put(neighborPixDCFKey + "_meanDeltaT", meanPixDCFDeltaT);
        input.put(neighborPixDCFKey + "_stdDevDeltaT", stdDevPixDCFDeltaT);
        input.put(neighborPixDCFKey + "_maxDeltaT", maxPixDCFDeltaT);
        input.put(neighborPixDCFKey + "_minDeltaT", minPixDCFDeltaT);

        return input;
    }


    /**
     * Calculate the descrete correlation function for a pair of values
     *
     * @param t        shift of the arrays
     * @param a        first array
     * @param b        second array
     * @param meanA    mean of a
     * @param meanB    mean of b
     * @param UDCFNorm
     * @return descrete correlation
     */
    public double DCF(int t, double[] a, double[] b, double meanA, double meanB, double UDCFNorm) {

        double dcf = 0.;
        int counter = 0;

        // Start with first slice of array a
        int start = 0;
        // Start with first slice of array b if t is negative
        if (t < 0) {
            start = Math.abs(t);
        }

        for (int i = start; i < a.length && i + t < b.length; i++) {

            dcf += UDCF(a[i], b[i + t], meanA, meanB, UDCFNorm);
            counter++;
        }

        return dcf / counter;
    }


    /**
     * Calculate the unbinned descrete correlation function for a pair of values
     *
     * @param a        first value
     * @param b        second value
     * @param meanA    mean of the a's origin
     * @param meanB    mean of the b's origin
     * @param UDCFNorm
     * @return unbinned descrete correlation
     */
    public double UDCF(double a, double b, double meanA, double meanB, double UDCFNorm) {
        // TODO: 15.08.16 if for some reason one of the UDCFNorm is zero you devide by zero
        return (a - meanA) * (b - meanB) / UDCFNorm;
    }


    /**
     * Calculate the norm for the unbinned descrete correlation function
     *
     * @param stdDevA standard deviation of a
     * @param stdDevB standard deviation of b
     * @param noiseA  noise of a
     * @param noiseB  noise of b
     * @return unbinned descrete correlation
     */
    public double UDCFNorm(double stdDevA, double stdDevB, double noiseA, double noiseB) {
        return Math.sqrt((stdDevA * stdDevA - noiseA * noiseA) * (stdDevB * stdDevB - noiseB * noiseB));
    }

    @Override
    public void init(ProcessContext context) throws Exception {
        default_noise = new double[Constants.N_PIXELS];
        Arrays.fill(default_noise, 0.0);
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }
}

