package fact.features.source;

import fact.coordinates.CameraCoordinate;
import fact.hexmap.ui.overlays.SourcePositionOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

//TODO: Documentation!?

public class AntiSourcePosition implements Processor {
    static Logger log = LoggerFactory.getLogger(AntiSourcePosition.class);

    @Parameter(required = true)
    public String sourcePositionKey = null;

    @Parameter(required = true)
    public int numberOfAntiSourcePositions;

    @Parameter(required = true)
    public int antiSourcePositionId;

    @Parameter(required = true)
    public String outputKey = null;

    @Override
    public Data process(Data input) {
        if (sourcePositionKey != null && !input.containsKey(sourcePositionKey)) {
            log.error("No source position in data item. Can not calculate anti source position!");
            throw new RuntimeException("Missing parameter. Enter valid sourcePositionKey");
        }
        CameraCoordinate source = (CameraCoordinate) input.get(sourcePositionKey);

        double rotAngle = 2 * Math.PI * antiSourcePositionId / (numberOfAntiSourcePositions + 1);

        CameraCoordinate antiSource = new CameraCoordinate(
                source.xMM * Math.cos(rotAngle) - source.yMM * Math.sin(rotAngle),
                source.xMM * Math.sin(rotAngle) + source.yMM * Math.cos(rotAngle)
        );

        input.put("@Source" + outputKey, new SourcePositionOverlay(outputKey, antiSource));
        input.put(outputKey, antiSource);
        input.put(outputKey + "X", antiSource.xMM);
        input.put(outputKey + "Y", antiSource.yMM);
        return input;
    }
}
