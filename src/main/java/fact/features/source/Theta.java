package fact.features.source;

import fact.Utils;
import fact.coordinates.CameraCoordinate;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class Theta implements Processor {
    @Parameter(required = true)
    public String sourcePositionKey = null;

    @Parameter(required = true)
    public String reconstructedPositionKey = null;

    @Parameter(required = true)
    public String outputKey = null;

    public Data process(Data input) {
        Utils.mapContainsKeys(input, sourcePositionKey, reconstructedPositionKey);

        CameraCoordinate sourcePosition = (CameraCoordinate) input.get(sourcePositionKey);
        CameraCoordinate reconstructedPosition = (CameraCoordinate) input.get(reconstructedPositionKey);

        double theta = sourcePosition.euclideanDistance(reconstructedPosition);
        input.put(outputKey, theta);

        return input;
    }

}
