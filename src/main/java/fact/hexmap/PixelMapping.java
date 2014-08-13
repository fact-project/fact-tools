/**
 * 
 */
package fact.hexmap;

/**
 *
 * @author kai
 */
public interface PixelMapping {

    public int getNumberOfPixel();
    public FactCameraPixel getPixelFromId(int id);
    public FactCameraPixel getPixelFromOffsetCoordinates(int x, int y);

}
