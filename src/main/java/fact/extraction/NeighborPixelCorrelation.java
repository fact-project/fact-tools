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
 * Calculate the average covariance and correlation of neighboring pixels to determine
 * the correlation of theire timeseries
 *
 * Created by jebuss on 04.04.16.
 */
public class NeighborPixelCorrelation implements Processor {
    @Parameter(required = true, description = "raw data array")
    private String key = null;

    @Parameter(required = true, description = "array containing the positions of maximum amplitudes for each pixel")
    private String amplitudePositionsKey = null;

    @Parameter(description = "Key of the pixel sample that should be used, " +
            "if no pixelset is given, the whole camera is used", defaultValue = "")
    private String pixelSetKey = null;

    @Parameter(description = "Number of slices to be skipped at the time lines beginning", defaultValue = "15")
    private int skipFirst = 15;

    @Parameter(description = "Number of slices to be skipped at the time lines end", defaultValue = "50")
    private int skipLast = 50;

    @Parameter(required = false, description = "Outputkey for the correlation of neighbouring pixels")
    private String correlationKey = "NPCor";

    @Parameter(required = false, description = "Outputkey for the covariance of neighbouring pixels")
    private String covarianceKey = "NPCov";

    @Parameter(required = false, description = "Outputkey for the covariance window marker")
    private String markerKey = "covarianceWindow";

    @Parameter(required = false, description = "Return scaled mean correlation (values between 0 and 1) if 'true'." +
            "Return absolute values if 'false'.", defaultValue = "false")
    private boolean returnScaledCorrelation = false;


    private int npix = 1440;
    private int roi = 300;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    // A logger
    static Logger log = LoggerFactory.getLogger(NeighborPixelCorrelation.class);

    @Override
    public Data process(Data input) {

        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");

        Utils.mapContainsKeys(input, key, amplitudePositionsKey);
        double[] data = (double[]) input.get(key);
        int[] amplitudePositions = (int[]) input.get(amplitudePositionsKey);
        int[] pixels = Utils.getValidPixelSetAsIntArr(input, npix, pixelSetKey);
        log.debug("npix: " + pixels.length );

        roi = data.length / npix;

        IntervalMarker[] m = new IntervalMarker[npix];

        //scale the data in the array to make it comparable
        double[] scaledData      = scaleData(data, amplitudePositions);

        //snip pixel Data to arrays without skipped slices
        double[][] snipedPixelData = Utils.snipPixelData(scaledData, skipFirst, skipLast, npix, roi);

        //get mean and variance of the timeseries for each pixel
        DescriptiveStatistics[] pixelStatistics = Utils.calculateTimeseriesStatistics(snipedPixelData);

        double[] covarianceMean     = new double[npix];
        double[] correlationMean    = new double[npix];

        double[] covarianceStd      = new double[npix];
        double[] correlationStd     = new double[npix];

        double[] covarianceMax      = new double[npix];
        double[] correlationMax     = new double[npix];

        double[] covarianceMin      = new double[npix];
        double[] correlationMin     = new double[npix];

        double[] covarianceKurtosis = new double[npix];
        double[] correlationKurtosis= new double[npix];

        double[] covarianceSkewness = new double[npix];
        double[] correlationSkewness= new double[npix];

        //Loop over all pixels to calculate the mean correlation with their neighbours
        for (int pix : pixels) {
            FactCameraPixel[] neighbours = pixelMap.getNeighboursFromID(pix);

            double pixVariance  = pixelStatistics[pix].getVariance();
            double pixMean      = pixelStatistics[pix].getMean();

            covarianceMean[pix]     = 0.;
            correlationMean[pix]    = 0.;

            DescriptiveStatistics statisticsCovariance = new DescriptiveStatistics();
            DescriptiveStatistics statisticsCorrelation = new DescriptiveStatistics();

            //Loop over all neighbour pixels to calculate the correlation with the given pixel
            for (CameraPixel neighbour : neighbours) {

                //exclude pixel that are not contained in the pixel set             }
                if (!ArrayUtils.contains(pixels, neighbour.id)){
                    continue;
                }

                double neighbourVariance    = pixelStatistics[neighbour.id].getVariance();
                double neighbourMean        = pixelStatistics[neighbour.id].getMean();

                double covariance  = calculateCovariance(scaledData, pix, pixMean,
                                                        neighbour, neighbourMean, skipFirst, skipLast);
                double correlation = calculateCorrelation(pixVariance, neighbourVariance, covariance);


                statisticsCovariance.addValue(covariance);
                statisticsCorrelation.addValue(correlation);
            }

            // weight with number of neighbours, (necessary for pixel at the camera edges and faulty pixels)

            covarianceMean[pix]     = statisticsCovariance.getMean();
            covarianceMax[pix]      = statisticsCovariance.getMax();
            covarianceMin[pix]      = statisticsCovariance.getMin();
            covarianceStd[pix]      = statisticsCovariance.getStandardDeviation();
            covarianceKurtosis[pix] = statisticsCovariance.getKurtosis();
            covarianceSkewness[pix] = statisticsCovariance.getSkewness();

            correlationMean[pix]     = statisticsCorrelation.getMean();
            correlationMax[pix]      = statisticsCorrelation.getMax();
            correlationMin[pix]      = statisticsCorrelation.getMin();
            correlationStd[pix]      = statisticsCorrelation.getStandardDeviation();
            correlationKurtosis[pix] = statisticsCorrelation.getKurtosis();
            correlationSkewness[pix] = statisticsCorrelation.getSkewness();

            correlationMean[pix] = statisticsCorrelation.getMean();

            m[pix] = new IntervalMarker(skipFirst,roi - skipLast);

        }

        if (returnScaledCorrelation == true) {
            input.put(correlationKey+"_mean", scaleCorrelation(correlationMean));
            input.put(correlationKey+"_max", scaleCorrelation(correlationMax));
            input.put(correlationKey+"_min", scaleCorrelation(correlationMin));
            input.put(correlationKey+"_stdDev", scaleCorrelation(correlationStd));
            input.put(correlationKey+"_Kurtosis", scaleCorrelation(correlationKurtosis));
            input.put(correlationKey+"_Skewness", scaleCorrelation(correlationSkewness));
        }
        else{
            input.put(correlationKey+"_mean", correlationMean);
            input.put(correlationKey+"_max", correlationMax);
            input.put(correlationKey+"_min", correlationMin);
            input.put(correlationKey+"_stdDev", correlationStd);
            input.put(correlationKey+"_Kurtosis", correlationKurtosis);
            input.put(correlationKey+"_Skewness", correlationSkewness);
        }

        input.put(markerKey, m);
        input.put(covarianceKey+"_mean", covarianceMean);
        input.put(covarianceKey+"_max", covarianceMax);
        input.put(covarianceKey+"_min", covarianceMin);
        input.put(covarianceKey+"_stdDev", covarianceStd);
        input.put(covarianceKey+"_Kurtosis", covarianceKurtosis);
        input.put(covarianceKey+"_Skewness", covarianceSkewness);

        return input;
    }

    private double calculateCorrelation(double pixVariance, double neighbourVariance, double covariance) {
        Double correlation = Math.abs(covariance) / Math.sqrt(pixVariance*pixVariance*neighbourVariance*neighbourVariance );
        return Math.abs(correlation);
    }

    private double calculateCovariance(double[] scaledData, int pix, double pixMean, CameraPixel neighbour,
                                       double neighbourMean, int skipFirst, int skipLast) {
        double covariance = 0.;

        for (int slice = 0 + skipFirst; slice < roi - skipLast; slice++) {
            double distancePixel        = scaledData[absPos(pix, slice)] - pixMean;
            double distanceNeighbour    = scaledData[absPos(neighbour.id, slice)] - neighbourMean;
            covariance += distancePixel * distanceNeighbour;
        }
        covariance /= roi;
        return covariance;
    }

    private double[] scaleData(double[] data, int[] amplitudePositions) {
        double[] scaledData = data.clone();
        for (int pix = 0; pix < npix; pix++) {
            int maxAmplPos = absPos(pix, amplitudePositions[pix]);
            double maxAmpl = data[maxAmplPos];

            //check if maxAmpl is 0 to avoid division by zero which leads to NaN values in scaledData.
            // Quick and dirty solution: if maxAmpl is close to 0, add an arbitrary 10 to every slice.
            //Other solution: do something with baseline. could be more elegant...
            if(Math.abs(maxAmpl) < 0.1){
                for (int slice = 0; slice < roi; slice++) {
                    scaledData[absPos(pix, slice)] = (scaledData[absPos(pix, slice)] + 10) / (maxAmpl+10);
                }
            }
            else {
                for (int slice = 0; slice < roi; slice++) {
                    scaledData[absPos(pix, slice)] = (scaledData[absPos(pix, slice)] / maxAmpl);
                }
            }
        }
        return scaledData;
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


    public void setKey(String key) {
        this.key = key;
    }

    public void setAmplitudePositionsKey(String amplitudePositionsKey) {
        this.amplitudePositionsKey = amplitudePositionsKey;
    }

    public void setSkipFirst(int skipFirst) {
        this.skipFirst = skipFirst;
    }

    public void setSkipLast(int skipLast) {
        this.skipLast = skipLast;
    }

    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    public void setCovarianceKey(String covarianceKey) {
        this.covarianceKey = covarianceKey;
    }

    public void setPixelSetKey(String pixelSetKey) {
        this.pixelSetKey = pixelSetKey;
    }

    public void setMarkerKey(String markerKey) {
        this.markerKey = markerKey;
    }

    public void setReturnScaledCorrelation(boolean returnScaledCorrelation) {this.returnScaledCorrelation = returnScaledCorrelation;}
}
