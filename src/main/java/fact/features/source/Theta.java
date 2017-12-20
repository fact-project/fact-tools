package fact.features.source;

import fact.Utils;
import fact.coordinates.CameraCoordinate;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class Theta implements Processor {
    @Parameter(required = true)
    private String sourcePositionKey = null;

    @Parameter(required = true)
    private String reconstructedPositionKey = null;

    @Parameter(required = true)
    private String outputKey = null;

    public Data process(Data input) {
        Utils.mapContainsKeys(input, sourcePositionKey, reconstructedPositionKey);

        CameraCoordinate sourcePosition = (CameraCoordinate) input.get(sourcePositionKey);
        CameraCoordinate reconstructedPosition = (CameraCoordinate) input.get(reconstructedPositionKey);

        double theta = sourcePosition.euclideanDistance(reconstructedPosition);
        input.put(outputKey, theta);

        return input;
    }


    public void setSourcePositionKey(String sourcePositionKey) {
        this.sourcePositionKey = sourcePositionKey;
    }

    public void setReconstructedPositionKey(String reconstructedPositionKey) {
        this.reconstructedPositionKey = reconstructedPositionKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }
}
