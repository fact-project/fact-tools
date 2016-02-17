package fact.features;

import fact.Utils;
import fact.container.PixelSet;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This processor calculates a kind of third moment along the longitudinal and
 * the transversal axis of the shower. Only kind of, because it is not normed
 * to units of the standard deviation, as it is for the correct definition
 * of the third moment.
 *
 * @author Fabian Temme
 */
public class M3Long implements Processor {
    public static final String Y = ":y";
    public static final String X = ":x";
    static Logger log = LoggerFactory.getLogger(M3Long.class);

    @Parameter(required = false, defaultValue = "pixels:estNumPhotons")
    private String estNumPhotonsKey = "pixels:estNumPhotons";
    @Parameter(required = false, description = "The key to the showerPixel. " +
            "That is some sort of int[] containing pixel chids.")
    private String pixelSetKey = "shower";
    @Parameter(required = false, defaultValue = "shower:ellipse:cog")
    private String cogKey = "shower:ellipse:cog";
    @Parameter(required = false, defaultValue = "shower:ellipse:delta")
    private String deltaKey = "shower:ellipse:delta";

    @Parameter(required = false, defaultValue = "shower:ellipse:m3l")
    private String m3lOutputKey = "shower:ellipse:m3l";
    @Parameter(required = false, defaultValue = "shower:ellipse:m3t")
    private String m3tOutputKey = "shower:ellipse:m3t";

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    @Override
    public Data process (Data item) {
        Utils.mapContainsKeys(item, estNumPhotonsKey, pixelSetKey, deltaKey,
                cogKey + X, cogKey + Y);

        int[] showerPixel = ((PixelSet) item.get(pixelSetKey)).toIntArray();
        double[] estNumPhotonsKeys = (double[]) item.get(estNumPhotonsKey);
        double[] showerWeights = createShowerWeights(showerPixel, estNumPhotonsKeys);
        double delta = (Double) item.get(deltaKey);
        double[] cog = {0, 0};
        cog[0] = (Double) item.get(cogKey + X);
        cog[1] = (Double) item.get(cogKey + Y);

        double[] sumd3w = calculateCubicSum(showerPixel, showerWeights, cog);
        double[] sumd2w = calculateSquaredSum(showerPixel, showerWeights, cog);

        double c = Math.cos(delta);
        double s = Math.sin(delta);
        double m3l = Math.pow(c, 3) * sumd3w[0] + Math.pow(s, 3) * sumd3w[1]
                + 3 * (s * c * c * sumd2w[0] + c * s * s * sumd2w[1]);
        double m3t = Math.pow(c, 3) * sumd3w[1] - Math.pow(s, 3) * sumd3w[0]
                + 3 * (s * s * c * sumd2w[0] - s * c * c * sumd2w[1]);

        item.put(m3lOutputKey, Math.cbrt(m3l));
        item.put(m3tOutputKey, Math.cbrt(m3t));

        return item;
    }

    private double[] calculateSquaredSum (int[] showerPixel, double[] showerWeights, double[] cog) {
        double[] sum2w = {0, 0};
        for (int i = 0; i < showerPixel.length; i++) {

            int pix = showerPixel[i];

            double dx = pixelMap.getPixelFromId(pix).getXPositionInMM() - cog[0];
            double dy = pixelMap.getPixelFromId(pix).getYPositionInMM() - cog[1];

            double tempValue = dx * dy * showerWeights[i];
            sum2w[0] += dx * tempValue;
            sum2w[1] += dy * tempValue;
        }
        return sum2w;
    }

    private double[] calculateCubicSum (int[] showerPixel, double[] showerWeights, double[] cog) {
        double[] sum3w = {0, 0};
        for (int i = 0; i < showerPixel.length; i++) {
            int pix = showerPixel[i];
            double dx = pixelMap.getPixelFromId(pix).getXPositionInMM() - cog[0];
            double dy = pixelMap.getPixelFromId(pix).getYPositionInMM() - cog[1];

            sum3w[0] += Math.pow(dx, 3) * showerWeights[i];
            sum3w[1] += Math.pow(dy, 3) * showerWeights[i];
        }
        return sum3w;
    }

    public double[] createShowerWeights (int[] shower, double[] pixelWeights) {
        double[] weights = new double[shower.length];
        for (int i = 0; i < shower.length; i++) {
            weights[i] = pixelWeights[shower[i]];
        }
        return weights;
    }

}
