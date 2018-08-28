package fact.features;

import fact.Utils;
import fact.container.PixelSet;
import fact.coordinates.CameraCoordinate;
import fact.hexmap.ui.overlays.EllipseOverlay;
import fact.statistics.weighted.Weighted1dStatistics;
import fact.statistics.weighted.Weighted2dStatistics;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Calculate the classical hillas parameters on a PixelSet.
 * Calculated are size, length, width, delta, the cog, and the higher moments m3, m4, skewness, kurtosis
 * each for longitudinal and transverse shower coordinates.
 */
public class HillasParameters implements Processor {

    @Parameter(required = true)
    public String weightsKey = null;

    @Parameter(required = true, description = "Key to the PixelSet that should be used for calculating the Hillas Parameters")
    public String pixelSetKey = null;

    @Parameter(required = false)
    public String sizeKey = "size";

    @Parameter(required = false, defaultValue = "cog")
    public String cogKey = "cog";

    @Parameter(required = false, defaultValue = "length")
    public String lengthKey = "length";

    @Parameter(required = false, defaultValue = "width")
    public String widthKey = "width";

    @Parameter(required = false, defaultValue = "delta")
    public String deltaKey = "delta";

    @Parameter(required = false, defaultValue = "skewness_long")
    public String skewnessLongKey = "skewness_long";

    @Parameter(required = false, defaultValue = "skewness_trans")
    public String skewnessTransKey = "skewness_trans";

    @Parameter(required = false, defaultValue = "kurtosis_long")
    public String kurtosisLongKey = "kurtosis_long";

    @Parameter(required = false, defaultValue = "kurtosis_trans")
    public String kurtosisTransKey = "kurtosis_trans";

    @Parameter(required = false, defaultValue = "m3_long")
    public String m3LongKey = "m3_long";

    @Parameter(required = false, defaultValue = "m3_trans")
    public String m3TransKey = "m3_trans";

    @Parameter(required = false, defaultValue = "m4_long")
    public String m4LongKey = "m4_long";

    @Parameter(required = false, defaultValue = "m4_trans")
    public String m4TransKey = "m4_trans";

    private static final Logger log = LoggerFactory.getLogger(HillasParameters.class);

    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, pixelSetKey, PixelSet.class);
        Utils.isKeyValid(item, weightsKey, double[].class);

        PixelSet showerPixel = (PixelSet) item.get(pixelSetKey);
        double[] weights = (double[])  item.get(weightsKey);

        double[] showerWeights = showerPixel.stream().mapToDouble((p) -> weights[p.id]).toArray();
        double[] pixelX = showerPixel.stream().mapToDouble((p) -> p.getXPositionInMM()).toArray();
        double[] pixelY = showerPixel.stream().mapToDouble((p) -> p.getYPositionInMM()).toArray();

        Weighted2dStatistics stats2d = Weighted2dStatistics.ofArrays(pixelX, pixelY, showerWeights);
        double size = stats2d.weightsSum;
        CameraCoordinate cog = new CameraCoordinate(stats2d.mean[0], stats2d.mean[1]);

        EigenDecomposition eig = new EigenDecomposition(stats2d.covarianceMatrix);
        double length = Math.sqrt(eig.getRealEigenvalue(0));
        double width = Math.sqrt(eig.getRealEigenvalue(1));
        double delta = calculateDelta(eig);

        // Calculation of the showers statistical moments (Variance, Skewness, Kurtosis)
        // Rotate the shower by the angle delta in order to have the ellipse
        // main axis in parallel to the Camera-Coordinates X-Axis
        double[] longitudinalCoordinates = new double[showerPixel.size()];
        double[] transverseCoordinates = new double[showerPixel.size()];


        for (int i = 0; i < showerPixel.size(); i++) {
            // translate to center
            double[] c = Utils.transformToEllipseCoordinates(pixelX[i], pixelY[i],  cog.xMM, cog.yMM, delta);
            // fill array of new shower coordinates
            longitudinalCoordinates[i] = c[0];
            transverseCoordinates[i] = c[1];
        }

        Weighted1dStatistics statsLong = Weighted1dStatistics.ofArrays(longitudinalCoordinates, showerWeights);
        Weighted1dStatistics statsTrans = Weighted1dStatistics.ofArrays(transverseCoordinates, showerWeights);

        item.put(sizeKey, size);
        item.put(lengthKey, length);
        item.put(widthKey, width);
        item.put(deltaKey, delta);

        item.put(cogKey, cog);
        item.put(cogKey + "_x", cog.xMM);
        item.put(cogKey + "_y", cog.yMM);

        item.put(m3LongKey, statsLong.m3);
        item.put(m3TransKey, statsTrans.m3);
        item.put(m4LongKey, statsLong.m4);
        item.put(m4TransKey, statsTrans.m4);

        item.put(skewnessLongKey, statsLong.skewness);
        item.put(skewnessTransKey, statsTrans.skewness);
        item.put(kurtosisLongKey, statsLong.kurtosis);
        item.put(kurtosisTransKey, statsTrans.kurtosis);

        item.put("1-sigma-ellipse", new EllipseOverlay(cog, length, width, delta));
        item.put("2-sigma-ellipse", new EllipseOverlay(cog, 2 * length,2 * width, delta));
        return item;
    }

    public double calculateDelta(EigenDecomposition eig) {
        // calculate the angle between the eigenvector and the camera axis.
        // So basicly the angle between the major-axis of the ellipse and the
        // camrera axis.
        // this will be written in radians.
        double longitudinalComponent = eig.getEigenvector(0).getEntry(0);
        double transverseComponent = eig.getEigenvector(0).getEntry(1);
        return Math.atan(transverseComponent / longitudinalComponent);
    }
}
