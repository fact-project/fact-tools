package fact.hexmap;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by kaibrugge on 23.04.14.
 */
public class FactCameraPixel extends CameraPixel implements Serializable {
    private static final long serialVersionUID = 7526472295622776147L;

    public int chid;
    public int board;
    public int softid;
    public int crate;
    public int patch;
    public int hardid;

    public double posX;
    public double posY;

    /**
     * This sets the chid of this pixel.
     * @param chid
     */
    public void setId(int chid){
        this.id = chid;
    }

    public void setHardid(int hardID) {
        this.hardid = hardID;
        this.crate = hardid / 1000;
        this.board = (hardid / 100) % 10;
        this.patch = (hardid / 10) % 10;
        this.chid  = (hardid % 10) + 9 * patch + 36 * board + 360 * crate;
        this.id = this.chid;
    }

    public void setSoftID(int softID) {
        this.softid = softID;
    }

    public double getXPositionInMM(){
        return posX*9.5;
    }
    public double getYPositionInMM(){
        return posY*9.5;
    }

    public double getXPositionInPixelUnits(){
        return posX;
    }
    public double getYPositionInPixelUnits(){
        return posY;
    }

    /**
     * This function returns the data contained in this pixel from the big data array containing the data for all pixels
     * @param data the array containing the data for all pixels
     * @param roi the region of interest in the data. Usually 300 slices or 1024
     * @return the data for this pixel
     */
    public double[] getPixelData(double[] data, int roi){
        double[] pixelData = Arrays.copyOfRange(data, id*roi, id*roi + roi);
        return pixelData;
    }


}
