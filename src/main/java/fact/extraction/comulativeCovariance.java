package fact.extraction;

import fact.Utils;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;

import java.util.Arrays;

/**
 * Caclutlate the comulative covariance of neighboring pixels to determine similarities
 *
 * Created by jebuss on 04.04.16.
 */
public class comulativeCovariance implements StatefulProcessor {
    @Parameter(required = true)
    private String key = null;

    @Parameter(required = true, description = "array containing the positions of maximum amplitudes for each pixel")
    private String amplitudePositionsKey = null;

    @Parameter(description = "Number of slices to be skipped at the time lines beginning", defaultValue = "50")
    private int skipFirst = 35;

    @Parameter(description = "Number of slices to be skipped at the time lines beginning", defaultValue = "50")
    private int skipLast = 100;

    @Parameter(required = false)
    private String scaledDataKey = "DataScaled";

    @Parameter(required = false)
    private String correlationKey = "meanCorrelation";

    @Parameter(required = false)
    private String covarianceKey = "meanCovariance";


    private int npix = 1440;
    private int roi = 300;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    //TODO add window handling
    //TODO add pixelset handling

    @Override
    public Data process(Data input) {

        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");

        Utils.mapContainsKeys(input, key, amplitudePositionsKey);
        double[] data = (double[]) input.get(key);
        int[] amplitudePositions = (int[]) input.get(amplitudePositionsKey);

        roi = data.length / npix;

        double[] meanCovariance     = new double[npix];
        double[] meanCorrelation    = new double[npix];

        double[]                scaledData      = scaleData(data, amplitudePositions);
        DescriptiveStatistics[] pixelStatistics = getTimeseriesStatistics(scaledData);

        //Loop over all pixels to calculate the correlation with their neighbours
        for (int pix = 0; pix < npix; pix++) {
            FactCameraPixel[] neighbours = pixelMap.getNeighboursFromID(pix);

            double pixVariance  = pixelStatistics[pix].getVariance();
            double pixMean      = pixelStatistics[pix].getMean();

            meanCovariance[pix]     = 0.;
            meanCorrelation[pix]    = 0.;

            //Loop over all neighbour pixels to calculate the correlation with the given pixel
            for (CameraPixel neighbour : neighbours) {

                double neighbourVariance    = pixelStatistics[neighbour.id].getVariance();
                double neighbourMean        = pixelStatistics[neighbour.id].getMean();

                double covariance = calculateCovariance(scaledData, pix, pixMean, neighbour, neighbourMean);

                meanCovariance[pix]     += covariance;
                meanCorrelation[pix]    += calculateCorrelation(pixVariance, neighbourVariance, covariance);
            }

            // weight with number of neighbours, (necessary for pixel at the camera fringe)
            meanCovariance[pix] /= neighbours.length;
            meanCorrelation[pix] /= neighbours.length;

        }

        input.put(covarianceKey, meanCovariance);
        input.put(correlationKey, meanCorrelation);
        input.put(scaledDataKey, scaledData);

        return input;
    }

    private double calculateCorrelation(double pixVariance, double neighbourVariance, double covariance) {
        Double correlation = Math.abs(covariance) / Math.sqrt(pixVariance*pixVariance*neighbourVariance*neighbourVariance );
        return Math.abs(correlation);
    }

    private double calculateCovariance(double[] scaledData, int pix, double pixMean, CameraPixel neighbour, double neighbourMean) {
        double covariance = 0.;

        for (int slice = 0; slice < roi; slice++) {
            double distancePixel        = scaledData[pix*roi+slice] - pixMean;
            double distanceNeighbour    = scaledData[neighbour.id*roi+slice] - neighbourMean;
            covariance += distancePixel * distanceNeighbour;
        }
        covariance /= roi;
        return covariance;
    }

    private DescriptiveStatistics[] getTimeseriesStatistics(double[] data) {
        DescriptiveStatistics[] pixelStatistics = new DescriptiveStatistics[npix];
        for (int pix = 0; pix < npix; pix++) {
            double[] scaledPixelData = Arrays.copyOfRange(data,pix*roi, (pix+1)*roi);
            pixelStatistics[pix] = new DescriptiveStatistics( scaledPixelData );
        }
        return pixelStatistics;
    }

    private double[] scaleData(double[] data, int[] amplitudePositions) {
        double[] scaledData = data.clone();
        for (int pix = 0; pix < npix; pix++) {
            int maxAmplPos = roi*pix + amplitudePositions[pix];
            double maxAmpl = data[maxAmplPos];

            for (int slice = 0; slice < roi; slice++) {
                scaledData[pix*roi+slice] /= maxAmpl;
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
}
