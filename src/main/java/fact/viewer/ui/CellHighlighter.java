/**
 * 
 */
package fact.viewer.ui;

import java.awt.Color;
import java.awt.Graphics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.image.overlays.Overlay;

/**
 * @author chris TODO: rewrite completly?
 */
public class CellHighlighter implements Overlay {

	static Logger log = LoggerFactory.getLogger(CellHighlighter.class);
	Integer x;
	Integer y;
	HexMap map;

	public CellHighlighter(HexMap map) {
		this.map = map;
	}

	public void highlight(int x, int y) {
		this.x = x;
		this.y = y;

	}

	public void clear() {
		x = null;
		y = null;
	}

	/**
	 * @see fact.image.overlays.Overlay#paint(java.awt.Graphics,
	 *      fact.viewer.ui.HexTile[][])
	 */
	@Override
	public void paint(Graphics g, HexTile[][] cells) {
		if (x == null || y == null)
			return;

		if (cells[x][y] != null)
			cells[x][y].paintBorder(g, Color.white);
	}

	@Override
	public void setCamMap(CameraPixelMap camMap) {
		// TODO Auto-generated method stub

	}
}