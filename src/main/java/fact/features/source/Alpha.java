package fact.features.source;

import fact.container.PixelDistribution2D;
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
    static Logger log = LoggerFactory.getLogger(Alpha.class);
    @Parameter(required = true)
    private String distribution = null;
    @Parameter(required = true)
    private String sourcePosition = null;
    @Parameter(required = true)
    private String outputKey = null;

    @Override
    public Data process(Data input) {
        PixelDistribution2D dist;
        try {
            dist = (PixelDistribution2D) input.get(distribution);
            if (dist == null) {
                log.info("No showerpixel in this event. Not calculating alpha");
                return input;
            }
        } catch (ClassCastException e) {
            log.error("distribution is not of type PixelDistribution2D. Aborting");
            return null;
        }


        CameraCoordinate source = null;
        try {
            source = (CameraCoordinate) input.get(sourcePosition);
            if (source == null) {
                throw new RuntimeException("This event didnt have a sourceposition. Eventnumber: " + input.get("EventNum"));
            }
        } catch (ClassCastException e) {
            log.error("wrong types" + e.toString());
        }

        double auxiliary_angle = Math.atan((source.yMM - dist.getCenterY()) / (source.xMM - dist.getCenterX()));

        double alpha = dist.getAngle() - auxiliary_angle;

        if (alpha > Math.PI / 2) {
            alpha = alpha - Math.PI;
        }
        if (alpha < -Math.PI / 2) {
            alpha = Math.PI + alpha;
        }
        input.put(outputKey, alpha);
        return input;
    }


    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }


    public String getSourcePosition() {
        return sourcePosition;
    }

    public void setSourcePosition(String sourcePosition) {
        this.sourcePosition = sourcePosition;
    }


    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }


}
