package fact.features.source;

import fact.Utils;
import fact.coordinates.CameraCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This feature is supposed to be the angle between the line defined by the major axis of the 2D distribution
 * (aka the shower ellipse) I have no idea.
 *
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class Alpha implements Processor {
    private static final Logger log = LoggerFactory.getLogger(Alpha.class);

    @Parameter(required = true)
    public String deltaKey;

    @Parameter(required = true)
    public String cogKey;

    @Parameter(required = true)
    public String sourcePositionKey;

    @Parameter(required = true)
    public String outputKey;

    @Override
    public Data process(Data item) {

        Utils.isKeyValid(item, cogKey, CameraCoordinate.class);
        Utils.isKeyValid(item, sourcePositionKey, CameraCoordinate.class);
        Utils.isKeyValid(item, deltaKey, Double.class);

        CameraCoordinate source = (CameraCoordinate) item.get(sourcePositionKey);
        CameraCoordinate cog = (CameraCoordinate) item.get(cogKey);
        double delta = (double) item.get(deltaKey);

        double auxiliary_angle = Math.atan2(source.yMM - cog.yMM, source.xMM - cog.xMM);
        double alpha = delta - auxiliary_angle;

        if (alpha > Math.PI / 2) {
            alpha = alpha - Math.PI;
        }
        if (alpha < -Math.PI / 2) {
            alpha = Math.PI + alpha;
        }
        item.put(outputKey, alpha);
        return item;
    }
}
