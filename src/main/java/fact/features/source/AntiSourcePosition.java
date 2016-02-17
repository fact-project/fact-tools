package fact.features.source;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

//TODO: Documentation!?

public class AntiSourcePosition implements Processor {
    public static final String Y = ":y";
    public static final String X = ":x";
    static Logger log = LoggerFactory.getLogger(AntiSourcePosition.class);

    @Parameter(required = false, defaultValue = "source")
    private String sourceKey = "source";
    @Parameter(required = false, defaultValue = "5")
    private int numberOfAntiSourcePositions = 5;
    @Parameter(required = true)
    private int antiSourcePositionId;
    @Parameter(required = false, description = "The outputkey for the " +
            "antiSourcePosition, if not specified antiSources:<id> is used")
    private String outputKey = null;

    @Override
    public Data process (Data item) {

        Utils.mapContainsKeys(item, sourceKey + X, sourceKey + Y);

        double sourcex = (Double) item.get(sourceKey + X);
        double sourcey = (Double) item.get(sourceKey + Y);

        double rotAngle = 2 * Math.PI * antiSourcePositionId / (numberOfAntiSourcePositions + 1);
        double antisourcex = sourcex * Math.cos(rotAngle) - sourcey * Math.sin(rotAngle);
        double antisourcey = sourcex * Math.sin(rotAngle) + sourcey * Math.cos(rotAngle);

        if (outputKey == null) {
            outputKey = "antiSources:" + antiSourcePositionId;
        }

        item.put(outputKey + X, antisourcex);
        item.put(outputKey + Y, antisourcey);
        return item;
    }
}
