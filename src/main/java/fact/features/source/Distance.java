package fact.features.source;

import fact.Utils;
import fact.coordinates.CameraCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Quite simply the distance between the CoG of the shower and the calculated source position.
 *
 * @author kaibrugge
 */
public class Distance implements Processor {
    private static final Logger log = LoggerFactory.getLogger(Distance.class);

    @Parameter(required = true)
    public String cogKey;

    @Parameter(required = true)
    public String sourcePositionKey;

    @Parameter(required = true)
    public String outputKey;

    /**
     * @return input. The original DataItem with a double named {@code outputKey}. Will return null one inputKey was invalid
     */
    @Override
    public Data process(Data item) {
        Utils.isKeyValid(item, cogKey, CameraCoordinate.class);
        Utils.isKeyValid(item, sourcePositionKey, CameraCoordinate.class);

        CameraCoordinate cog = (CameraCoordinate) item.get(cogKey);
        CameraCoordinate source = (CameraCoordinate) item.get(sourcePositionKey);

        item.put(outputKey, cog.euclideanDistance(source));
        return item;
    }
}
