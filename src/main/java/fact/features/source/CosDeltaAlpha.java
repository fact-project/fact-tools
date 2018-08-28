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
    public String cogKey;

    @Parameter(required = true)
    public String deltaKey;

    @Parameter(required = true)
    public String outputKey;

    @Override
    public Data process(Data item) {

        Utils.mapContainsKeys(item, sourcePositionKey, cogKey, deltaKey);
        Utils.isKeyValid(item, sourcePositionKey, CameraCoordinate.class);
        Utils.isKeyValid(item, cogKey, CameraCoordinate.class);

        CameraCoordinate sourcePosition = (CameraCoordinate) item.get(sourcePositionKey);
        CameraCoordinate cog = (CameraCoordinate) item.get(cogKey);
        double delta = (double) item.get(deltaKey);

        double dist = cog.euclideanDistance(sourcePosition);

        double cosDeltaAlpha;
        if (dist == 0) {
            cosDeltaAlpha = Double.NaN;
        } else {

            double s = Math.sin(delta);
            double c = Math.cos(delta);

            double arg2 = c * (cog.xMM - sourcePosition.xMM) + s * (cog.yMM - sourcePosition.yMM); // mm

            if (arg2 == 0) {
                cosDeltaAlpha = Double.NaN;
            } else {
                cosDeltaAlpha = arg2 / dist;
            }
        }

        item.put(outputKey, cosDeltaAlpha);
        return item;
    }
}
