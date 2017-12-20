package fact.features.source;

import fact.Utils;
import fact.container.PixelDistribution2D;
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
    static Logger log = LoggerFactory.getLogger(Distance.class);
    @Parameter(required = true)
    private String distribution;
    @Parameter(required = true)
    private String sourcePosition;
    @Parameter(required = true)
    private String outputKey;

    /**
     * @return input. The original DataItem with a double named {@code outputKey}. Will return null one inputKey was invalid
     */
    @Override
    public Data process(Data input) {
        if (!input.containsKey(distribution)) {
            log.info("No shower in event. Not calculating distance");
            return input;
        }
        Utils.mapContainsKeys(input, distribution, sourcePosition);

        PixelDistribution2D dist = (PixelDistribution2D) input.get(distribution);
        CameraCoordinate source = (CameraCoordinate) input.get(sourcePosition);

        double dx = dist.getCenterX() - source.xMM;
        double dy = dist.getCenterY() - source.yMM;

        input.put(outputKey, Math.sqrt(Math.pow(dx, 2.0) + Math.pow(dy, 2.0)));
        return input;
    }


    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public void setSourcePosition(String sourcePosition) {
        this.sourcePosition = sourcePosition;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }


}
