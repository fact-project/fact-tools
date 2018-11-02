package fact.hexmap;

import fact.Constants;
import fact.coordinates.CameraCoordinate;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by kaibrugge on 23.04.14.
 */
public class CameraPixel implements Serializable {
    private static final long serialVersionUID = 7526472295622776147L;

    public final int id;
    public final int geometricX;
    public final int geometricY;

    public final int chid;
    public final int board;
    public final int softid;
    public final int crate;
    public final int patch;
    public final int hardid;
    public final int drs_chip;
    public final CameraCoordinate coordinate;

    @Override
    public String toString() {
        return "ID: " + this.id;
    }

    public CameraPixel(int softID, int hardID, double posX, double posY, int geometricX, int geometricY){
        this.hardid = hardID;
        this.softid = softID;
        this.crate = hardID / 1000;
        this.board = (hardID / 100) % 10;
        this.patch = (hardID / 10) % 10;
        this.chid = (hardID % 10) + 9 * patch + 36 * board + 360 * crate;
        this.drs_chip = this.chid / 9;
        this.id = this.chid;
        this.geometricX = geometricX;
        this.geometricY = geometricY;
        this.coordinate = new CameraCoordinate(posX * Constants.PIXEL_SIZE_MM, posY * Constants.PIXEL_SIZE_MM);
    }
}
