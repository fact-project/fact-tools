package fact.features.source;

import fact.Constants;
import fact.Utils;
import fact.coordinates.CameraCoordinate;
import fact.coordinates.HorizontalCoordinate;
import fact.hexmap.ui.overlays.SourcePositionOverlay;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import sun.management.HotspotRuntimeMBean;

public class ReconstructSourcePositionDisp implements Processor {
    @Parameter(required=true)
    private String dispKey = null;
    @Parameter(required=true)
    private String cogxKey = null;
    @Parameter(required=true)
    private String cogyKey = null;
    @Parameter(required=true)
    private String deltaKey = null;
    @Parameter(required=true)
    private String m3lKey = null;
    @Parameter(required=true)
    private String cosDeltaAlphaKey = null;
    @Parameter(required=true)
    private String outputKey = null;
    @Parameter(required=true)
    private double signM3lConstant = 0;

    public Data process(Data input) {
        Utils.mapContainsKeys(input, dispKey, cogxKey, cogyKey, deltaKey, cosDeltaAlphaKey);

        double disp = (Double) input.get(dispKey);
        double cogx = (Double) input.get(cogxKey);
        double cogy = (Double) input.get(cogyKey);
        double delta = (Double) input.get(deltaKey);
        double m3l = (Double) input.get(m3lKey);
        double cosDeltaAlpha = (Double) input.get(cosDeltaAlphaKey);

        CameraCoordinate recPosition = calculateRecPosition(cogx, cogy, disp, delta, cosDeltaAlpha, m3l);

        input.put("@reconstructedPosition" + outputKey, new SourcePositionOverlay(outputKey, recPosition));
        input.put(outputKey, recPosition);

        return input;
    }

    private CameraCoordinate calculateRecPosition(double cogx, double cogy, double disp, double delta, double cosDeltaAlpha, double m3l) {
        // The orientation of the reconstructed source position depends on the third moment
        // (relativ to the suspected source position, m3l*sign(cosDeltaAlpha)) of the shower:
        // If it is larger than a constant (default -200) the reconstructed source position is
        // orientated towards the suspected source position
        double sign = - Math.signum(cosDeltaAlpha) * Math.signum(m3l * Math.signum(cosDeltaAlpha) - signM3lConstant);

        double x = cogx + disp * Math.cos(delta) * sign;
        double y = cogy + disp * Math.sin(delta) * sign;

        return new CameraCoordinate(x, y);
    }

    public void setDispKey(String dispKey) {
        this.dispKey = dispKey;
    }

    public void setCogxKey(String cogxKey) {
        this.cogxKey = cogxKey;
    }

    public void setCogyKey(String cogyKey) {
        this.cogyKey = cogyKey;
    }

    public void setDeltaKey(String deltaKey) {
        this.deltaKey = deltaKey;
    }

    public void setM3lKey(String m3lKey) {
        this.m3lKey = m3lKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public void setCosDeltaAlphaKey(String cosDeltaAlphaKey) {
        this.cosDeltaAlphaKey = cosDeltaAlphaKey;
    }

    public void setSignM3lConstant(double signM3lConstant) {
        this.signM3lConstant = signM3lConstant;
    }

}
