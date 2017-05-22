package fact.coordinates;

/**
 * Created by maxnoe on 22.05.17.
 */
public class CameraCoordinate {
    private final double xMM;
    private final double yMM;

    public CameraCoordinate(double xMM, double yMM){
        this.xMM = xMM;
        this.yMM = yMM;
    }

    public double getxMM() {
        return xMM;
    }

    public double getyMM() {
        return yMM;
    }
}
