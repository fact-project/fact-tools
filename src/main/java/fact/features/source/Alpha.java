package fact.features.source;

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This processor calculates the angle between the main axis of the shower and the line cog <-> sourcePosition
 *
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;Fabian Temme
 */
public class Alpha implements Processor {
    public static final String Y = ":y";
    public static final String X = ":x";
    static Logger log = LoggerFactory.getLogger(Alpha.class);

    @Parameter(required = false, defaultValue = "shower:ellipse:cog")
    private String cogKey = "shower:ellipse:cog";
    @Parameter(required = false, defaultValue = "shower:ellipse:delta")
    private String deltaKey = "shower:ellipse:delta";
    @Parameter(required = false, defaultValue = "source")
    private String sourceKey = "source";
    @Parameter(required = false, defaultValue = "shower:source:alpha")
    private String outputKey = "shower:source:alpha";

    @Override
    public Data process (Data item) {

        Utils.mapContainsKeys(item,
                sourceKey + X, sourceKey + Y,
                cogKey + X, cogKey + Y, deltaKey);

        double sourcex = (Double) item.get(sourceKey + X);
        double sourcey = (Double) item.get(sourceKey + Y);

        double cogx = (Double) item.get(cogKey + X);
        double cogy = (Double) item.get(cogKey + Y);
        double delta = (Double) item.get(deltaKey);

        double auxiliary_angle = Math.atan((sourcey - cogy) / (sourcex - cogx));
        double alpha = (delta - auxiliary_angle);

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
