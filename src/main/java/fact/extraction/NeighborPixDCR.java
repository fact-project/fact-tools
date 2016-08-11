package fact.extraction;

import fact.Utils;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.Arrays;

/**
 * Calculate the Descrete Correlation Function for the Time Series of neigbouring pixels as a measure for their
 * correllation
 *
 * Created by jebuss on 10.08.16.
 */
public class NeighborPixDCR implements Processor {
    @Parameter(required = true, description = "raw data array")
    private String key = null;

    @Parameter(description = "Key of the pixel sample that should be used, " +
            "if no pixelset is given, the whole camera is used", defaultValue = "")
    private String pixelSetKey = null;

    @Parameter(description = "Number of slices to be skipped at the time lines beginning", defaultValue = "15")
    private int skipFirst = 15;

    @Parameter(description = "Number of slices to be skipped at the time lines beginning", defaultValue = "50")
    private int skipLast = 50;

    @Parameter(required = false, description = "Outputkey for the mean correlation of neighbouring pixels")
    private String correlationKey = "meanCorrelation";

    @Parameter(required = false, description = "Outputkey for the mean covariance of neighbouring pixels")
    private String covarianceKey = "meanCovariance";

    @Parameter(required = false, description = "Outputkey for the covariance window marker")
    private String markerKey = "covarianceWindow";

    @Parameter(required = false, description = "Return scaled mean correlation (values between 0 and 1) if 'true'. Return absolute values if 'false'.", defaultValue = "false")
    private boolean returnScaledCorrelation = false;


    private int npix = 1440;
    private int roi = 300;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    // A logger
    static Logger log = LoggerFactory.getLogger(NeighborPixDCR.class);

    @Override
    public Data process(Data input) {

        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");

        double[] data = (double[]) input.get(key);
        int[] pixels = Utils.getValidPixelSetAsIntArr(input, npix, pixelSetKey);
        log.debug("npix: " + pixels.length);

        roi = data.length / npix;

        IntervalMarker[] m = new IntervalMarker[npix];

        //snip pixel Data to arrays without skipped slices
        double[][] snippedPixelData = snipPixelData(data, skipFirst, skipLast);

        //get mean and variance of the timeseries for each pixel
        DescriptiveStatistics[] pixelStatistics = getTimeseriesStatistics(snippedPixelData);

        int deltaTMax = 0;

        //Loop over all pixels to calculate the mean correlation with their neighbours
        for (int pix : pixels) {
            FactCameraPixel[] neighbours = pixelMap.getNeighboursFromID(pix);

            double pixStdDev = pixelStatistics[pix].getStandardDeviation();
            double pixMean = pixelStatistics[pix].getMean();

            int numNeighbours = neighbours.length;


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

                for (int t : deltaT) {
                    dcf[deltaTMax + t] = DCF(t, snippedPixelData[pix], snippedPixelData[neighbour.id], pixMean,
                                                neighbourMean, pixStdDev, neighbourStdDev, 0.0, 0.0);
                }


            }


//            m[pix] = new IntervalMarker(skipFirst, roi - skipLast);

        }


//        input.put(markerKey, m);
//        input.put(covarianceKey, meanCovariance);

        return input;
    }

    private double DCF(int t, double[] a, double[] b, double meanA, double meanB, double stdDevA, double stdDevB,
                       double noiseA, double noiseB) {

        double dcf = 0.;
        int counter = 0;

        // Start with first slice of array a
        int start = 0;
        // Start with first slice of array b if t is negative
        if (t < 0) {
            start = Math.abs(t);
        }

        for (int i = start; i < a.length && i + t < b.length; i++) {
            dcf += UDCF(a[1], b[i + t], meanA, meanB, stdDevA, stdDevB, noiseA, noiseB);
            counter++;
        }

        return dcf / counter;
    }


    private double UDCF(double a, double b, double meanA, double meanB, double stdDevA, double stdDevB,
                        double noiseA, double noiseB) {
        double udcf = (a - meanA) * (b - meanB);
        udcf /= Math.sqrt((stdDevA * stdDevA - noiseA * noiseA) * (stdDevB * stdDevB - noiseB * noiseB));


        return udcf;
    }

    private int absPos(int pix, int slice) {
        return pix * roi + slice;
    }

    private double[][] snipPixelData(double[] data, int skipFirst, int skipLast){

        double[][] pixelData = new double[npix][];
        for (int pix = 0; pix < npix; pix++) {
            pixelData[pix] = Arrays.copyOfRange(data, absPos(pix, skipFirst), absPos(pix + 1, (-1) * skipLast));
        }

        return pixelData;

    }

    private DescriptiveStatistics[] getTimeseriesStatistics(double[][] snippedPixelData) {
        DescriptiveStatistics[] pixelStatistics = new DescriptiveStatistics[npix];

        for (int pix = 0; pix < npix; pix++) {
            pixelStatistics[pix] = new DescriptiveStatistics(snippedPixelData[pix]);
        }
        return pixelStatistics;
    }
}

