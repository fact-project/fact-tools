/**
 *
 */
package fact.hexmap;

/**
 * @author kai
 */
public interface PixelMapping {

    public int getNumberOfPixel();

    public CameraPixel getPixelFromId(int id);

    public CameraPixel getPixelFromOffsetCoordinates(int x, int y);

}
