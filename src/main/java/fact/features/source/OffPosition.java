package fact.features.source;

import fact.Utils;
import fact.coordinates.CameraCoordinate;
import fact.hexmap.ui.overlays.SourcePositionOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

//TODO: Documentation!?

public class OffPosition implements Processor {
    static Logger log = LoggerFactory.getLogger(OffPosition.class);

    @Parameter(required = true)
    public String sourcePositionKey = null;

    @Parameter(required = true)
    public int numberOfOffPositions;

    @Parameter(required = true)
    public int offPositionId;

    @Parameter(required = true)
    public String outputKey = null;

    @Override
    public Data process(Data input) {
        Utils.isKeyValid(input, sourcePositionKey, CameraCoordinate.class);

        CameraCoordinate source = (CameraCoordinate) input.get(sourcePositionKey);

        double rotAngle = 2 * Math.PI * offPositionId / (numberOfOffPositions + 1);

        CameraCoordinate offPosition = new CameraCoordinate(
                source.xMM * Math.cos(rotAngle) - source.yMM * Math.sin(rotAngle),
                source.xMM * Math.sin(rotAngle) + source.yMM * Math.cos(rotAngle)
        );

        input.put(outputKey + "Marker", new SourcePositionOverlay(outputKey, offPosition));
        input.put(outputKey, offPosition);
        input.put(outputKey + "X", offPosition.xMM);
        input.put(outputKey + "Y", offPosition.yMM);
        return input;
    }
}