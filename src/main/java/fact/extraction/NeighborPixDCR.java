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
        log.debug("npix: " + pixels.length );

        roi = data.length / npix;

        IntervalMarker[] m = new IntervalMarker[npix];

        //get mean and variance of the timeseries for each pixel
        DescriptiveStatistics[] pixelStatistics = getTimeseriesStatistics(data, skipFirst, skipLast);

        double[] meanCovariance     = new double[npix];
        double[] meanCorrelation    = new double[npix];

        //Loop over all pixels to calculate the mean correlation with their neighbours
        for (int pix : pixels) {
            FactCameraPixel[] neighbours = pixelMap.getNeighboursFromID(pix);

            double pixVariance  = pixelStatistics[pix].getVariance();
            double pixMean      = pixelStatistics[pix].getMean();

            int numNeighbours = neighbours.length;

            //Loop over all neighbour pixels to calculate the correlation with the given pixel
            for (CameraPixel neighbour : neighbours) {

                //exclude pixel that are not contained in the pixel set             }
                if (!ArrayUtils.contains(pixels, neighbour.id)   ){
                    numNeighbours -= 1;
                    continue;
                }

                double neighbourVariance    = pixelStatistics[neighbour.id].getVariance();
                double neighbourMean        = pixelStatistics[neighbour.id].getMean();


            }



            m[pix] = new IntervalMarker(skipFirst,roi - skipLast);

        }

        if (returnScaledCorrelation == true) {
            double[] scaledCorrelation = scaleCorrelation(meanCorrelation);
            input.put(correlationKey, scaledCorrelation);
        }
        else{
            input.put(correlationKey, meanCorrelation);
        }

        input.put(markerKey, m);
        input.put(covarianceKey, meanCovariance);

        return input;
    }

    private double DCF(double tau, double[] a, double[] b, double meanA, double meanB, double stdDevA, double stdDevB){

    }


    private double UDCF(double a, double b, double meanA, double meanB, double stdDevA, double stdDevB,
                        double noiseA, double noiseB) {
        double udcf =  (a-meanA)*(b-meanB);
        udcf /= Math.sqrt((stdDevA*stdDevA - noiseA*noiseA)*(stdDevB*stdDevB-noiseB*noiseB));

        return udcf;
    }

    private double UDCF(double a, double b, double meanA, double meanB, double stdDevA, double stdDevB){
        return UDCF(a, b, meanA, meanB, stdDevA, stdDevB, 0.0, 0.0);
    }

    private int absPos(int pix, int slice) {
        return pix*roi+slice;
    }

    private DescriptiveStatistics[] getTimeseriesStatistics(double[] data, int skipFirst, int skipLast) {
        DescriptiveStatistics[] pixelStatistics = new DescriptiveStatistics[npix];

        for (int pix = 0; pix < npix; pix++) {
            int left_edge   = absPos(pix, skipFirst);
            int right_edge  = absPos(pix+1, (-1)*skipLast);
            double[] scaledPixelData = Arrays.copyOfRange(data, left_edge, right_edge);
            pixelStatistics[pix] = new DescriptiveStatistics( scaledPixelData );
        }
        return pixelStatistics;
    }


    private double[] scaleCorrelation(double[] correlation){

        double[] scaledCorrelation = new double[1440];
        double max = 0;
        double min = 1000;
        for(int i=0; i<1440; i++){
            if(correlation[i] > max){
                int maxIndex = i;
                max = correlation[i];
            }
            if(correlation[i] < min){
                int minIndex = i;
                min = correlation[i];
            }
        }


        for(int i=0; i<1440; i++){
            scaledCorrelation[i] = (correlation[i] - min) / (max - min);
        }

        return scaledCorrelation;
    }

