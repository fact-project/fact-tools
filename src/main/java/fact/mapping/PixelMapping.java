/**
 * 
 */
package fact.mapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;

/**
 *
 * @author kai
 */
public interface PixelMapping {

    public int getNumberOfPixel();
    public FactCameraPixel getPixelFromId(int id);
    public FactCameraPixel getPixelFromOffsetCoordinates(int x, int y);

}
