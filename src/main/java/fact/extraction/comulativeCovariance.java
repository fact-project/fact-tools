package fact.extraction;

import fact.Utils;
import fact.hexmap.CameraPixel;
import fact.hexmap.FactCameraPixel;
import fact.hexmap.FactPixelMapping;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import stream.Data;
import stream.ProcessContext;
import stream.Processor;
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
    private String outputKey = "DataScaled";


    private int npix = 1440;

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    @Override
    public Data process(Data input) {

        Utils.isKeyValid(input, "NPIX", Integer.class);
        npix = (Integer) input.get("NPIX");

        Utils.mapContainsKeys(input, key, amplitudePositionsKey);
        double[] data = (double[]) input.get(key);
        int[] amplitudePositions = (int[]) input.get(amplitudePositionsKey);

        int roi = data.length / npix;
        double[] scaledData = data.clone();

        DescriptiveStatistics[] pixelStatistics = new DescriptiveStatistics[npix];
        double[][] scaledPixelData = new double[npix][];

        double[] comulativeCovariance = new double[npix];
        double[] comulativeCorelation = new double[npix];

        for (int pix = 0; pix < npix; pix++) {
            int maxAmplPos = roi*pix + amplitudePositions[pix];
            double maxAmpl = data[maxAmplPos];

            for (int slice = 0; slice < roi; slice++) {
                scaledData[pix*roi+slice] /= maxAmpl;
            }

            scaledPixelData[pix] = Arrays.copyOfRange(scaledData,pix*roi, (pix+1)*roi);
            pixelStatistics[pix] = new DescriptiveStatistics( scaledPixelData[pix] );
        }

//        Till this point we scaled all timeserise to the max amplitude in each pixel. Now I would like to calculate the covariance of neightborpixel time series
        for (int pix = 0; pix < npix; pix++) {
            FactCameraPixel[] neighbours = pixelMap.getNeighboursFromID(pix);

            double pixVariance  = pixelStatistics[pix].getVariance();
            double pixMean      = pixelStatistics[pix].getMean();

            comulativeCovariance[pix] = 0.;
            comulativeCorelation[pix] = 0.;

            for (CameraPixel neighbour :
                    neighbours) {

                double neighbourVariance    = pixelStatistics[neighbour.id].getVariance();
                double neighbourMean        = pixelStatistics[neighbour.id].getMean();


                double covariance = 0.;

                for (int slice = 0; slice < roi; slice++) {
                    double distancePixel = scaledPixelData[pix][slice] - pixMean;
                    double distanceNeighbour = scaledPixelData[neighbour.id][slice] - neighbourMean;
                    covariance += distancePixel * distanceNeighbour;
                }
                covariance /= roi;

                comulativeCovariance[pix] += covariance;
                comulativeCorelation[pix] += covariance / Math.sqrt(pixVariance*pixVariance*neighbourVariance*neighbourVariance );
            }
        }

        input.put("comulativeCovariance", comulativeCovariance);
        input.put("comulativeCorelation", comulativeCorelation);

//            double[] scaledPixelData = Arrays.copyOfRange(scaledData,pix*roi, (pix+1)*roi - 1);

        input.put(outputKey, scaledData);

        return input;
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

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }
}
