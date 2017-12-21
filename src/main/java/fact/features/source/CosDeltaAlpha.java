package fact.features.source;

import fact.Utils;
import fact.coordinates.CameraCoordinate;
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
    @Parameter(required = true)
    public String sourcePositionKey;

    @Parameter(required = true)
    public String cogxKey;

    @Parameter(required = true)
    public String cogyKey;

    @Parameter(required = true)
    public String deltaKey;

    @Parameter(required = true)
    public String outputKey;

    @Override
    public Data process(Data input) {

        Utils.mapContainsKeys(input, sourcePositionKey, cogxKey, cogyKey, deltaKey);
        CameraCoordinate sourcePosition = (CameraCoordinate) input.get(sourcePositionKey);

        double cogx = (double) input.get(cogxKey);
        double cogy = (double) input.get(cogyKey);
        double delta = (double) input.get(deltaKey);

        double sx = cogx - sourcePosition.xMM;
        double sy = cogy - sourcePosition.yMM;
        double dist = Math.sqrt(sx * sx + sy * sy);

        if (dist == 0) {
            return input;
        }

        double s = Math.sin(delta);
        double c = Math.cos(delta);

        double arg2 = c * sx + s * sy; // mm

        if (arg2 == 0) {
            return input;
        }
        //double arg1 = c*sy - s*sx;          // [mm]

        double cosDeltaAlpha = arg2 / dist;

        input.put(outputKey, cosDeltaAlpha);
        return input;
    }
}
