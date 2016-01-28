package fact.hexmap;

import java.io.Serializable;
import java.util.Arrays;

/**
 *
 * A special CameraPixel containing fact specific ids.
 * @see fact.hexmap.CameraPixel
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
    public int drs_chip;

    public double posX;
    public double posY;

    /**
     * This sets the chid of this pixel.
     * @param chid the chid to set.
     */
    public void setId(int chid){
        this.id = chid;
    }

    /**
     * This will automatically set crate,board,patch,chid and drs_chip. All these values can be inferred from the hardid
     * @param hardID the id to set.
     */
    public void setHardid(int hardID) {
        this.hardid = hardID;
        this.crate = hardid / 1000;
        this.board = (hardid / 100) % 10;
        this.patch = (hardid / 10) % 10;
        this.chid  = (hardid % 10) + 9 * patch + 36 * board + 360 * crate;
        this.drs_chip = this.chid / 9;
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
