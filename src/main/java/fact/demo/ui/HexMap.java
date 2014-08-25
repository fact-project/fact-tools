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

import fact.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	final ColorMapping colors = new RainbowColorMapping(); // new
															// NeutralColorMapping();
	final FactHexTile[] tiles = new FactHexTile[pixelMapping.getNumberOfPixel()];

	int currentSlice = 0;
	double[][] sliceValues = new double[pixelMapping.getNumberOfPixel()][300];

	double minValue = 1000.0;
	double maxValue = 0.0;

	final AtomicBoolean playing = new AtomicBoolean(false);
	final DecimalFormat fmt = new DecimalFormat("000");

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

//		int slices = data.length / 1440;
//		log.info("Data has {} slices", slices);


        this.sliceValues = Utils.sortPixels(data, 1440);
        for (double[] slices : sliceValues) {
            for (double v : slices) {
                minValue = Math.min(minValue, v);
                maxValue = Math.max(maxValue, v);
            }
        }

//		for (int i = 0; i < data.length && i < sliceValues.length; i++) {
//			for (int slice = 0; slice < sliceValues[i].length; slice++) {
//				double val = data[i * slices + slice];
//				sliceValues[i][slice] = val;
//				minValue = Math.min(minValue, val);
//				maxValue = Math.max(maxValue, val);
//			}
//		}
    }

	/**
	 * @see javax.swing.JComponent#print(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		super.paint(g);

		g.setColor(this.getBackground());
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		int xOffset = getWidth() / 2 + 0;
		int yOffset = getHeight() / 2 + 0;
		if (g instanceof Graphics2D) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			g2.setStroke(new BasicStroke(2.0f));
			g.setColor(Color.DARK_GRAY);
			Double begin = 10.0;
			Double end = getWidth() - 10.0;
			g.drawLine(10, getHeight() - 6, getWidth() - 10, getHeight() - 6);

			double slices = sliceValues[0].length;
			double s = currentSlice;

			Double pos = s * ((end - begin) / slices);

			Font f = g.getFont().deriveFont(9.0f);
			g.setFont(f);
			g.drawString("Slice: " + fmt.format(currentSlice), getWidth() - 54,
					getHeight() - 12);
			g.setColor(Color.red);
			g.drawLine(10 + pos.intValue(), getHeight(), 10 + pos.intValue(),
					getHeight() - 8);

			// draw a grid with lines every 25 pixel in a dark grey color
			g2.setStroke(new BasicStroke(1.0f));
			g2.setColor(Color.DARK_GRAY);
			// drawGrid(g2, 25);

			// now draw the actual camera pixel
			// translate to center of canvas
			g2.translate(xOffset, yOffset);
			// rotate 90 degrees counter clockwise
			g2.rotate(-Math.PI / 2);
			// and draw tiles

			for (Tile tile : tiles) {
				// CameraPixel p = tile.getCameraPixel();
				// int slice = currentSlice;
				// if (currentSlice >=
				// sliceValues[tile.getCameraPixel().id].length) {
				// slice = sliceValues[tile.getCameraPixel().id].length - 1;
				// }
				// double value = sliceValues[tile.getCameraPixel().id][slice];
				// tile.setFillColor(colors.getColorFromValue(value, minValue,
				// maxValue));

				double value = sliceValues[tile.getCameraPixel().id][currentSlice];
				Color color = colors.getColorFromValue(value, minValue,
						maxValue);

				tile.setFillColor(color);
				tile.setBorderColor(Color.BLACK);
				tile.paint(g);
			}

			// draw all overlays
			// for (CameraMapOverlay o : overlays) {
			// o.paint(g2, this);
			// }

		}
	}

	/**
	 * @return the currentSlice
	 */
	public int getCurrentSlice() {
		return currentSlice;
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
}
