/**
 * 
 */
package fact.demo.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Utils;
import fact.hexmap.FactPixelMapping;
import fact.hexmap.ui.colormapping.ColorMapping;
import fact.hexmap.ui.colormapping.RainbowColorMapping;
import fact.hexmap.ui.components.cameradisplay.FactHexTile;
import fact.hexmap.ui.components.cameradisplay.Tile;

/**
 * @author chris
 * 
 */
public class HexMap extends JPanel {

	/** The unique class ID */
	private static final long serialVersionUID = 2423044746282665853L;

	static Logger log = LoggerFactory.getLogger(HexMap.class);

	final FactPixelMapping pixelMapping = FactPixelMapping.getInstance();
	final ColorMapping colors = new RainbowColorMapping();

	final FactHexTile[] tiles = new FactHexTile[pixelMapping.getNumberOfPixel()];

	int currentSlice = 0;
	double[][] sliceValues = new double[pixelMapping.getNumberOfPixel()][300];

	double minValue = 1000.0;
	double maxValue = 0.0;

	boolean timeScale = true;

	final AtomicBoolean playing = new AtomicBoolean(false);
	final DecimalFormat fmt = new DecimalFormat("000");
	private int[] showerIds;

	public HexMap() {
		setBackground(Color.white);
		for (int i = 0; i < tiles.length; i++) {
			FactHexTile t = new FactHexTile(pixelMapping.getPixelFromId(i), 3.5);
			tiles[i] = t;
			for (int s = 0; s < sliceValues[i].length; s++) {
				sliceValues[i][s] = 0.0d;
			}
		}
	}

	public void setRadius(double rad) {
		for (int i = 0; i < tiles.length; i++) {
			FactHexTile t = new FactHexTile(pixelMapping.getPixelFromId(i), rad);
			tiles[i] = t;
		}
	}

	public void setData(double[] data) {
		this.sliceValues = Utils.sortPixels(data, 1440);
		for (double[] slices : sliceValues) {
			for (double v : slices) {
				minValue = Math.min(minValue, v);
				maxValue = Math.max(maxValue, v);
			}
		}
	}

	/**
	 * @see javax.swing.JComponent#print(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);

		g.setColor(this.getBackground());
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		int xOffset = getWidth() / 2;
		int yOffset = getHeight() / 2;

		if (g instanceof Graphics2D) {

			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			if (timeScale) {
				g2.setStroke(new BasicStroke(2.0f));
				g2.setColor(Color.DARK_GRAY);

				Double begin = 10.0;
				Double end = getWidth() - 10.0;

				g2.drawLine(10, getHeight() - 6, getWidth() - 10,
						getHeight() - 6);

				double slices = sliceValues[0].length;
				double s = currentSlice;

				Double pos = s * ((end - begin) / slices);

				Font f = g2.getFont().deriveFont(9.0f);
				g2.setFont(f);
				g2.drawString("Slice: " + fmt.format(currentSlice),
						getWidth() - 54, getHeight() - 12);
				g2.setColor(Color.red);
				g2.drawLine(10 + pos.intValue(), getHeight(),
						10 + pos.intValue(), getHeight() - 8);
			}

			g2.setStroke(new BasicStroke(1.0f));
			g2.setColor(Color.DARK_GRAY);

			// draw a grid with lines every 25 pixel in a dark grey color
			// drawGrid(g2, 25);

			// translate to center of canvas
			g2.translate(xOffset, yOffset);
			// rotate 90 degrees counter clockwise
			g2.rotate(-Math.PI / 2);

			// In case we have shower pixel draw the camera in gray except for
			// the camera pixel
			if (showerIds != null && showerIds.length > 0) {
				for (Tile tile : tiles) {
					tile.setBorderColor(Color.lightGray);
					tile.setFillColor(Color.lightGray);
					tile.paint(g);
				}
				for (int pix : showerIds) {
					Tile tile = tiles[pix];
					double value = sliceValues[tile.getCameraPixel().id][currentSlice];
					Color color = colors.getColorFromValue(value, minValue,
							maxValue);
					tile.setFillColor(color);
					tile.setBorderColor(Color.BLACK);
					tile.paint(g);
				}
			} else {
				// no shower so just draw all the pixels in color
				for (Tile tile : tiles) {
					double value = sliceValues[tile.getCameraPixel().id][currentSlice];
					Color color = colors.getColorFromValue(value, minValue,
							maxValue);

					tile.setFillColor(color);
					tile.setBorderColor(Color.BLACK);
					tile.paint(g);
				}
			}

			// draw all overlays
			// for (CameraMapOverlay o : overlays) {
			// o.paint(g2, this);
			// }
		}
	}

	/**
	 * @param currentSlice
	 *            the currentSlice to set
	 */
	public void setCurrentSlice(int currentSlice) {
		this.currentSlice = currentSlice;
	}

	public void play(final long interval) {
		if (playing.get()) {
			log.warn("Panel is already playing...");
			return;
		}
		startPlaying();

		Thread t = new Thread() {
			public void run() {
				try {
					for (int s = 0; s < 300; s++) {
						setCurrentSlice(s);
						repaint();
						Thread.sleep(interval);
					}
				} catch (Exception e) {
				}
				setCurrentSlice(0);
				stopPlaying();
			}
		};

		t.start();
	}

	private void startPlaying() {
		playing.set(true);
	}

	private void stopPlaying() {
		playing.set(false);

		try {
			this.notifyAll();
		} catch (Exception e) {
			log.error("Failed to notifyAll(): {}", e.getMessage());
		}
	}

	public boolean isPlaying() {
		return playing.get();
	}

	public void setShowerIds(int[] showerIds) {
		this.showerIds = showerIds;
	}

	/**
	 * @return the timeScale
	 */
	public boolean isTimeScale() {
		return timeScale;
	}

	/**
	 * @param timeScale
	 *            the timeScale to set
	 */
	public void setTimeScale(boolean timeScale) {
		this.timeScale = timeScale;
	}

}
