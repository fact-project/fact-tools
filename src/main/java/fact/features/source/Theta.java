package fact.features.source;

import fact.Utils;
import fact.hexmap.ui.overlays.SourcePositionOverlay;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class Theta implements Processor {
    public static final String X = ":x";
    public static final String Y = ":y";

    @Parameter(required = false, defaultValue = "source")
    private String sourceKey = "source";
    @Parameter(required = false, defaultValue = "shower:disp")
    private String dispKey = "shower:disp";
    @Parameter(required = false, defaultValue = "shower:ellipse:cog")
    private String cogKey = "shower:ellipse:cog";
    @Parameter(required = false, defaultValue = "shower:ellipse:delta")
    private String deltaKey = "shower:ellipse:delta";
    @Parameter(required = false, defaultValue = "shower:ellipse:m3l")
    private String m3lKey = "shower:ellipse:m3l";
    @Parameter(required = false, defaultValue = "shower:ellipse:delta")
    private String cosDeltaAlphaKey = "shower:source:cosDeltaAlpha";
    @Parameter(required = false, defaultValue = "shower:source:theta")
    private String outputKey = "shower:source:theta";
    @Parameter(required = false, defaultValue = "-200")
    private double signM3lConstant = -200;

    public Data process (Data item) {
        Utils.mapContainsKeys(item, sourceKey + X, sourceKey + Y, dispKey,
                cogKey + X, cogKey + Y, deltaKey, m3lKey, cosDeltaAlphaKey);

        double sourcex = (Double) item.get(sourceKey + X);
        double sourcey = (Double) item.get(sourceKey + Y);
        double disp = (Double) item.get(dispKey);
        double cogx = (Double) item.get(cogKey + X);
        double cogy = (Double) item.get(cogKey + Y);
        double delta = (Double) item.get(deltaKey);
        double m3l = (Double) item.get(m3lKey);
        double cosDeltaAlpha = (Double) item.get(cosDeltaAlphaKey);

        double[] recPosition = calculateRecPosition(cogx, cogy, disp, delta, m3l, cosDeltaAlpha);
        double theta = Math.sqrt(Math.pow(recPosition[0] - sourcex, 2)
                + Math.pow(recPosition[1] - sourcey, 2));

        item.put("gui:sourceOverlay:reconstructedPosition:" + outputKey,
                new SourcePositionOverlay(
                        "gui:sourceOverlay:reconstructedPosition:" + outputKey,
                        recPosition));
        item.put(outputKey + ":recPos" + X, recPosition[0]);
        item.put(outputKey + ":recPos" + Y, recPosition[1]);
        item.put(outputKey, theta);

        return item;
    }

    /**
     * Calculate the reconstructed source position. The orientation of the
     * reconstructed source position depends on the third moment (relativ to
     * the suspected source position, m3l*sign(cosDeltaAlpha)) of the shower.
     * If it is larger than a constant (default -200) the reconstructed source
     * position is orientated towards the suspected source position.
     */
    private double[] calculateRecPosition (double cogx, double cogy,
                                           double disp, double delta,
                                           double m3l, double cosDeltaAlpha) {

        double[] result = new double[2];

        double sign = -Math.signum(cosDeltaAlpha) *
                Math.signum(m3l * Math.signum(cosDeltaAlpha) - signM3lConstant);

        result[0] = cogx + disp * Math.cos(delta) * sign;
        result[1] = cogy + disp * Math.sin(delta) * sign;

        return result;
    }
}
