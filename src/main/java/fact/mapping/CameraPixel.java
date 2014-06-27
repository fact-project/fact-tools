package fact.mapping;

/**
 * This describes the stuff a pixel to be drawn on the getColorFromValue must have. This should be subclassed if you need
 * more information in each pixel
 * Created by kaibrugge on 23.04.14.
 */
public class CameraPixel {

    public int id;
    public int geometricX;
    public int geometricY;
    public double width = 1;
    public double length = 1;

    @Override
    public String toString(){
        return "ID: " + this.id ;
    }


}
