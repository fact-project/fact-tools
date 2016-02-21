package fact.features.source;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This processor calculates CosDeltaAlpha from MARS Code:
 * fCosDeltaAlpha cosine of angle between d and a, where
 * - d is the vector from the source position to the
 * center of the ellipse
 * - a is a vector along the main axis of the ellipse,
 * defined with positive x-component
 *
 * @author Jan Freiwald
 */
public class CosDeltaAlpha implements Processor {


    public static final String X = ":x";
    public static final String Y = ":y";
    @Parameter(required = false, defaultValue = "source")
    private String sourceKey = "source";
    @Parameter(required = false, defaultValue = "shower:ellipse:cog:x")
    private String cogKey = "shower:ellipse:cog";
    @Parameter(required = false, defaultValue = "shower:ellipse:delta")
    private String deltaKey = "shower:ellipse:delta";
    @Parameter(required = false, defaultValue = "shower:source:cosDeltaAlpha")
    private String outputKey = "shower:source:cosDeltaAlpha";

    @Override
    public Data process (Data item) {

        Utils.mapContainsKeys(item, sourceKey + X, sourceKey + Y,
                cogKey + X, cogKey + Y, deltaKey);

        double sourcex = (Double) item.get(sourceKey + X);
        double sourcey = (Double) item.get(sourceKey + Y);

        double cogx = (Double) item.get(cogKey + X);
        double cogy = (Double) item.get(cogKey + Y);
        double delta = (Double) item.get(deltaKey);

        double sx, sy, dist;
        sx = cogx - sourcex;
        sy = cogy - sourcey;
        dist = Math.sqrt(sx * sx + sy * sy);

        if (dist == 0) {
            return item;
        }

        double s = Math.sin(delta);
        double c = Math.cos(delta);

        double arg2 = c * sx + s * sy; // mm

        if (arg2 == 0) {
            return item;
        }

        //double arg1 = c*sy - s*sx;          // [mm]

        double cosDeltaAlpha = arg2 / dist;

        item.put(outputKey, cosDeltaAlpha);
        return item;
    }
}
