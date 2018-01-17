package fact.features.source;

import fact.Utils;
import fact.coordinates.CameraCoordinate;
import fact.hexmap.ui.overlays.SourcePositionOverlay;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class ReconstructSourcePositionDisp implements Processor {
    @Parameter(required = true)
    public String dispKey = null;

    @Parameter(required = true)
    public String cogKey = null;


    @Parameter(required = true)
    public String deltaKey = null;

    @Parameter(required = true)
    String m3LongKey = null;

    @Parameter(required = true)
    public String cosDeltaAlphaKey = null;

    @Parameter(required = true)
    public String outputKey = null;

    @Parameter(required = true)
    public double signM3lConstant = 0;

    public Data process(Data input) {
        Utils.mapContainsKeys(input, dispKey, cogKey,  deltaKey, cosDeltaAlphaKey, m3LongKey);
        Utils.isKeyValid(input, cogKey, CameraCoordinate.class);

        double disp = (double) input.get(dispKey);
        CameraCoordinate cog = (CameraCoordinate) input.get(cogKey);

        double delta = (double) input.get(deltaKey);
        double m3l = Math.cbrt((double) input.get(m3LongKey));  // MARS calls cbrt(M3) M3
        double cosDeltaAlpha = (double) input.get(cosDeltaAlphaKey);

        CameraCoordinate recPosition = calculateRecPosition(cog.xMM, cog.yMM, disp, delta, cosDeltaAlpha, m3l);

        input.put(outputKey + "Marker", new SourcePositionOverlay(outputKey, recPosition));
        input.put(outputKey, recPosition);
        input.put(outputKey + 'X', recPosition.xMM);
        input.put(outputKey + 'Y', recPosition.yMM);

        return input;
    }

    /**
     * The orientation of the reconstructed source position depends on the third moment
     * (relativ to the suspected source position, m3l*sign(cosDeltaAlpha)) of the shower:
     * If it is larger than a constant (default -200) the reconstructed source position is
     * orientated towards the suspected source position
     *
     * @param cogx
     * @param cogy
     * @param disp
     * @param delta
     * @param cosDeltaAlpha
     * @param m3l
     * @return CameraCoordinate with the reconstructed source position
     */
    private CameraCoordinate calculateRecPosition(double cogx, double cogy, double disp, double delta, double cosDeltaAlpha, double m3l) {

        double sign = -Math.signum(cosDeltaAlpha) * Math.signum(m3l * Math.signum(cosDeltaAlpha) - signM3lConstant);

        double x = cogx + disp * Math.cos(delta) * sign;
        double y = cogy + disp * Math.sin(delta) * sign;

        return new CameraCoordinate(x, y);
    }
}
