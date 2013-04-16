/**
 * 
 */
package fact.image.overlays;

import java.awt.Graphics;

import fact.viewer.ui.CameraPixelMap;
import fact.viewer.ui.HexTile;

/**
 * An overlay simply provides additional information for a hex-tile
 * and can draw above the camera-pixel map.
 * 
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;   Kai Bruegge  &lt;kai.bruegge@udo.edu&gt;
 *
 */
public interface Overlay {

	/**
	 * This method is called after the camera grid has been painted and
	 * can be used to repaint or overdraw any of the cells.
	 * 
	 * @param g
	 * @param cells
	 */
	public void paint( Graphics g, HexTile[][] cells );
	
	
	/**
	 * To draw itself the overlay might need to know which CameraMapView its being painted on. 
	 * Different CameraMapViews can have different sizes. (The size in pixels which appears on the screen)
	 * That means any overlay that draws above the Map might need to Transform some arbritary coordinates 
	 * into PixelCoordinates depending on the CameraMapView its being painted on.
	 * 
	 * @param camMap
	 */
	
	public void setCamMap(CameraPixelMap camMap);
}
