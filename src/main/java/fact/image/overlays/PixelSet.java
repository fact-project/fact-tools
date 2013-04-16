/**
 * 
 */
package fact.image.overlays;

import java.awt.Color;
import java.awt.Graphics;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.image.Pixel;
import fact.viewer.ui.CameraPixelMap;
import fact.viewer.ui.HexTile;

/**
 * @author chris
 * 
 */
public class PixelSet extends TreeSet<Pixel> implements Overlay {

	/** The unique class ID */
	private static final long serialVersionUID = 695408813710438087L;

	static Logger log = LoggerFactory.getLogger(PixelSet.class);

	private static int lastColor = 0;
	final static Color[] colors = new Color[] { Color.RED, Color.BLUE,
			Color.YELLOW, Color.GREEN };

	Color color = Color.RED;

	public PixelSet() {
		this(colors[lastColor++ % colors.length]);
	}

	public PixelSet(Color c) {
		this.color = c;
	}

	/**
	 * @return the color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @param color
	 *            the color to set
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * @see fact.image.overlays.Overlay#paint(java.awt.Graphics,
	 *      fact.viewer.ui.HexTile[][])
	 */
	@Override
	public void paint(Graphics g, HexTile[][] cells) {
		for (Pixel pix : this) {
			if (pix.x > 0 && pix.x < cells.length) {
				if (pix.y > 0 && pix.y < cells[pix.x].length) {
					HexTile tile = cells[pix.x][pix.y];
					if (tile != null) {
						log.trace("Drawing tile at ({},{})", pix.x, pix.y);
						tile.paintBorder(g, color);
//						tile.paintBackground(g, color);
					}
				}
			}
		}
	}

	public boolean contains(int softId) {
		return this.contains(new Pixel(softId));
	}

	@Override
	public void setCamMap(CameraPixelMap camMap) {
		// TODO Auto-generated method stub
		
	}
}