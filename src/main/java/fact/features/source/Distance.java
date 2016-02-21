package fact.features.source;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Quite simply the distance between the CoG of the shower and the source position.
 *
 * @author kaibrugge, Fabian Temme
 */
public class Distance implements Processor {
    public static final String X = ":x";
    public static final String Y = ":y";
    static Logger log = LoggerFactory.getLogger(Distance.class);

    @Parameter(required = false, defaultValue = "shower:ellipse:cog")
    private String cogKey = "shower:ellipse:cog";
    @Parameter(required = false, defaultValue = "source")
    private String sourceKey = "source";
    @Parameter(required = false, defaultValue = "shower:source:distance")
    private String outputKey = "shower:source:distance";

    @Override
    public Data process (Data item) {
        Utils.mapContainsKeys(item, cogKey + X, cogKey + Y,
                sourceKey + X, sourceKey + Y);

        double sourcex = (Double) item.get(sourceKey + X);
        double sourcey = (Double) item.get(sourceKey + Y);

        double cogx = (Double) item.get(cogKey + X);
        double cogy = (Double) item.get(cogKey + Y);

        double distance = Math.sqrt(Math.pow((cogx - sourcex), 2) + Math.pow((cogy - sourcey), 2));

        item.put(outputKey, distance);
        return item;
    }
}
