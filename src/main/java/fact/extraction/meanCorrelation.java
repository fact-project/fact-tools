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
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

import java.util.Arrays;

/**
 * Caclutlate the average covariance and correlation of neighboring pixels to determine
 * the correlation of theire timeseries
 *
 * Created by jebuss on 04.04.16.
 */
public class meanCorrelation implements StatefulProcessor {
    @Parameter(required = true, description = "raw data array")
    private String key = null;

    @Parameter(required = true, description = "array containing the positions of maximum amplitudes for each pixel")
    private String amplitudePositionsKey = null;

    @Parameter(description = "Key of the pixel sample that should be used, " +
            "if no pixelset is given, the whole camera is used", defaultValue = "")
    private String pixelSetKey;

    @Parameter(description = "Number of slices to be skipped at the time lines beginning", defaultValue = "15")
    private int skipFirst = 15;

    @Parameter(description = "Number of slices to be skipped at the time lines beginning", defaultValue = "50")
    private int skipLast = 50;

    @Parameter(required = false, description = "Outputkey for the scaled Data array")
    private String scaledDataKey = "DataScaled";

    @Parameter(required = false, description = "Outputkey for the mean correlation of neighbouring pixels")
    private String correlationKey = "meanCorrelation";

    @Parameter(required = false, description = "Outputkey for the mean covariance of neighbouring pixels")
    private String covarianceKey = "meanCovariance";

    @Parameter(required = false, description = "Outputkey for the covariance window marker")
    private String markerKey = "covarianceWindow";


    private int npix = 1440;
    private int roi = 300;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    // A logger
    static Logger log = LoggerFactory.getLogger(WaveformFluctuation.class);

    @Override
    public Data process(Data input) {

        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");

        Utils.mapContainsKeys(input, key, amplitudePositionsKey);
        double[] data = (double[]) input.get(key);
        int[] amplitudePositions = (int[]) input.get(amplitudePositionsKey);
        int[] pixels = Utils.getValidPixelSet(input, npix, pixelSetKey);
        log.debug("npix: " + pixels.length );

        roi = data.length / npix;

        IntervalMarker[] m = new IntervalMarker[npix];

        //scale the data in the array to make it comparable
        double[] scaledData      = scaleData(data, amplitudePositions);

        //get mean and variance of the timeseries for each pixel
        DescriptiveStatistics[] pixelStatistics = getTimeseriesStatistics(scaledData, skipFirst, skipLast);

        double[] meanCovariance     = new double[npix];
        double[] meanCorrelation    = new double[npix];

        //Loop over all pixels to calculate the mean correlation with their neighbours
        for (int pix : pixels) {
            FactCameraPixel[] neighbours = pixelMap.getNeighboursFromID(pix);

            double pixVariance  = pixelStatistics[pix].getVariance();
            double pixMean      = pixelStatistics[pix].getMean();

            meanCovariance[pix]     = 0.;
            meanCorrelation[pix]    = 0.;

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

                double covariance = calculateCovariance(scaledData, pix, pixMean,
                                                        neighbour, neighbourMean, skipFirst, skipLast);

                meanCovariance[pix]     += covariance;
                meanCorrelation[pix]    += calculateCorrelation(pixVariance, neighbourVariance, covariance);
            }

            // weight with number of neighbours, (necessary for pixel at the camera edges and faulty pixels)
            meanCovariance[pix] /= numNeighbours;
            meanCorrelation[pix] /= numNeighbours;

            m[pix] = new IntervalMarker(skipFirst,roi - skipLast);

        }

        input.put(markerKey, m);
        input.put(covarianceKey, meanCovariance);
        input.put(correlationKey, meanCorrelation);
        input.put(scaledDataKey, scaledData);

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

    private double[] scaleData(double[] data, int[] amplitudePositions) {
        double[] scaledData = data.clone();
        for (int pix = 0; pix < npix; pix++) {
            int maxAmplPos = absPos(pix, amplitudePositions[pix]);
            double maxAmpl = data[maxAmplPos];

            for (int slice = 0; slice < roi; slice++) {
                scaledData[absPos(pix, slice)] /= maxAmpl;
            }
        }
        return scaledData;
    }

    @Override
    public void init(ProcessContext context) throws Exception {

    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

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

    public void setScaledDataKey(String scaledDataKey) {
        this.scaledDataKey = scaledDataKey;
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
}
