/**
 * 
 */
package fact.viewer.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Point;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.metadata.IIOMetadataNode;
import javax.swing.Action;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.Constants;
import fact.image.overlays.Overlay;
import fact.viewer.SelectionListener;
import fact.viewer.colorMappings.ColorMapping;
import fact.viewer.colorMappings.NeutralColorMapping;

/**
 * @author chris
 * 
 */
public class HexMap extends JPanel implements PixelMap {
	/** The unique class ID */
	private static final long serialVersionUID = -4015808725138908874L;

	final static Point[] neighbors = new Point[] { new Point(-1, -1),
			new Point(-1, 0), new Point(-1, 1), new Point(0, 1),
			new Point(1, 1), new Point(1, 0), new Point(1, -1) };

	static Logger log = LoggerFactory.getLogger(HexMap.class);

	HexTile tiles[][]; // = new HexTile[][];
	Panel p;

	// initialize the hexagonal grid.
	int rows = 10;
	int cols = 10;
	Double cellHeight;
	Double cellWidth;
	public Double cellRadius;
	Color borderColor = Color.BLACK; // Color.BLUE;
	
	ColorMapping colorMap = new NeutralColorMapping();
	HexTile cellBySoftId[] = new HexTile[1440];
	
	List<SelectionListener> selectionListener = new ArrayList<SelectionListener>();
	Set<HexTile> selectedTiles = new LinkedHashSet<HexTile>();
	Set<HexTile> annotatedTiles = new LinkedHashSet<HexTile>();

	List<Action> mapActions = new ArrayList<Action>();

	List<Overlay> overlays = new ArrayList<Overlay>();
	
	private double minValue, maxValue;
	private int roi;
	public double[][] sliceValues = new double[1440][300];
//	float[][] transformed = new float[1440][1];
	double[] average = new double[300];
	int currentSlice;
	
	DefaultPixelMapping defaultPixelMapping = new DefaultPixelMapping();

	// protected final List<Overlay> eventOverlays = new ArrayList<Overlay>();

	public HexMap(int rows, int cols, Double radius) {
		cellRadius = radius;
		cellHeight = Math.sqrt(3) * radius;
		cellWidth = 2 * radius;
		this.rows = rows;
		this.cols = cols;

		tiles = new HexTile[rows][cols];
	}

	public HexTile[] getCells() {
		return new HexTile[0];
	}

	public double getCellRadius() {
		return cellRadius;
	}

	public boolean hasCell(int i, int j) {
		if (i > 0 && j > 0 && i < tiles.length && j < tiles[i].length) {
			return tiles[i][j] != null;
		}
		return false;
	}

	public List<HexTile> getNeighbors(int i, int j) {
		List<HexTile> neighs = new ArrayList<HexTile>();
		for (Point p : neighbors) {
			if (hasCell(i + p.x, j + p.y))
				neighs.add(getCell(i + p.x, j + p.y));
		}

		return neighs;
	}

	/**
	 * @see javax.swing.JComponent#getHeight()
	 */
	@Override
	public int getHeight() {
		return (int) (this.cellHeight * (rows - 1));
	}

	/**
	 * @see javax.swing.JComponent#getMinimumSize()
	 */
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(getWidth(), getHeight()); // getHeight(),
														// getWidth() );
	}

	/**
	 * @see javax.swing.JComponent#getWidth()
	 */
	@Override
	public int getWidth() {
		return (int) (cellWidth * (cols - 1));
	}

	/**
	 * @see javax.swing.JComponent#getMaximumSize()
	 */
	@Override
	public Dimension getMaximumSize() {
		return getMinimumSize();
	}

	/**
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	/**
	 * @return the borderColor
	 */
	public Color getBorderColor() {
		return borderColor;
	}

	/**
	 * @param borderColor
	 *            the borderColor to set
	 */
	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	// The paint method.
	public void paint(Graphics g) {

		if (borderColor == null)
			borderColor = Color.BLACK;

		g.setColor(this.getBackground());
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		// System.out.println("Dimensions:  " + this.getWidth() + ", " +
		// this.getHeight());
		// g.setColor( this.borderColor );
		// g.fillRect( rect.x, rect.y, rect.width, rect.height );

		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				if (tiles[i][j] != null) {
					tiles[i][j].setBorderColor(Color.BLACK);
					tiles[i][j].paint(g);
				}
			}
		}

		paintSelected(g);
		paintAnnotated(g);

		//paint the 0,0 point in the camera
		Point p = getPixelCoordsFromRealCoords(0, 0);

		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.WHITE);
		g2.setStroke(new BasicStroke(2.0f));
		g2.draw(new Ellipse2D.Double(p.x, p.y, 10.0, 10));

		
		for (Overlay overlay : overlays) {
			overlay.paint(g, tiles);
		}

	}

	/***
	 * assuming input values are in [mm] millimeters
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Point getPixelCoordsFromRealCoords(double x, double y) {

		int xOff = (int) ((getWidth() * 0.5)) - 43;
		int yOff = (int) ((getHeight() * 0.5)) - 23;

		return new Point(xOff + ((int) x), yOff + ((int) y));
	}

	public void paintTiles(Graphics g) {
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				if (tiles[i][j] != null) {
					tiles[i][j].setBorderColor(Color.BLACK);
					tiles[i][j].paint(g);
				}
			}
		}
	}

	public void paintTiles(Graphics g, double[] values, ColorMapping cm) {
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				if (tiles[i][j] != null) {
					// tiles[i][j].setBorderColor( Color.BLACK );
					Integer id = tiles[i][j].getId();
					if (id != null && i >= 0 && i < values.length) {
						tiles[i][j].paintBackground(g,  cm.map( (float) values[id], getMinValue(), getMaxValue() )  );
					} else
						tiles[i][j].paint(g);
				}
			}
		}
	}
	
	
	
	public void setData(double[] slices) {
		
		if (slices == null) {
			log.error("No data found in event!");
			return;
		}
		minValue = Double.MAX_VALUE;
		maxValue = Double.MIN_VALUE;

		roi = slices.length / Constants.NUMBEROFPIXEL;
		average = new double[roi];
		sliceValues = new double[Constants.NUMBEROFPIXEL][roi];
		double[][] values = defaultPixelMapping.sortPixels(slices);


		for (int row = 0; row < sliceValues.length && row < values.length; row++) {
			for (int s = 0; s < sliceValues[row].length && s < values[row].length; s++) {
				sliceValues[row][s] = values[row][s];
				if (!Double.isNaN(sliceValues[row][s])) {
					minValue = Math.min(minValue, sliceValues[row][s]);
					maxValue = Math.max(maxValue, sliceValues[row][s]);
				}
				average[s] += sliceValues[row][s];
			}
		}

		log.debug("minimum is: {}, maximum is: {}", minValue, maxValue);
		
		for (int i = 0; i < average.length; i++) {
			average[i] = average[i] / sliceValues.length;
		}

		log.debug("Slices loaded.");
		setCurrentSlice(currentSlice);
		repaint();
	}
	
	public void setCurrentSlice(int i) {
		log.debug("Selecting slice: {}", i);
		if (i >= 0 && i < sliceValues[0].length) {
			currentSlice = i;
			for (int p = 0; p < sliceValues.length; p++) {
				if (getCellById(p) != null)
					getCellById(p).setColor(colorMap.map(sliceValues[p][i], this.getMinValue(), this.getMaxValue()));
			}
			this.repaint();
		}
	}

	public void paintSelected(Graphics g) {
		for (HexTile tile : selectedTiles) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(2.0f));
			tile.paintBorder(g, Color.RED);
		}
	}

	public void paintAnnotated(Graphics g) {
		for (HexTile tile : annotatedTiles) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(2.0f));
			tile.paintBorder(g, Color.BLUE);
		}
	}

	public void clearSelection() {
		selectedTiles.clear();
		repaint();
	}

	public Set<Integer> getSelectedIds() {
		Set<Integer> ids = new LinkedHashSet<Integer>();
		for (HexTile cell : selectedTiles) {
			ids.add(cell.getId());
		}
		return ids;
	}

	public void addToSelection(HexTile cell) {
		selectedTiles.add(cell);

		for (SelectionListener l : selectionListener) {
			log.info("Notifying selection-listener {}", l);
			l.itemSelected(this, getSelectedIds());
		}
	}

	// this seems to be broken.
	public void setSelectedIds(Set<Integer> select) {
		selectedTiles.clear();
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				if (tiles[i][j] != null) {

					selectedTiles.add(tiles[i][j]);
				}
			}
		}
		for (SelectionListener l : selectionListener) {
			log.info("Notifying selection-listener {}", l);
			l.itemSelected(this, select);
		}
	}

	public void updateSelectedIds(Set<Integer> select) {
		for (SelectionListener l : selectionListener) {
			log.info("Notifying selection-listener {}", l);
			l.itemSelected(this, select);
		}
	}

	public boolean isSelected(int i, int j) {
		Iterator<HexTile> it = selectedTiles.iterator();
		while (it.hasNext()) {
			HexTile p = it.next();
			if (p.getGeoX() == i && p.getGeoY() == j) {
				it.remove();
				return true;
			}
		}
		return false;
	}

	public void setAnnotatedTiles(Set<HexTile> cells) {
		this.annotatedTiles.clear();
		this.annotatedTiles.addAll(cells);
	}

	public boolean isSelected(HexTile cell) {
		return selectedTiles.contains(cell);
	}

	public void removeFromSelection(HexTile cell) {
		selectedTiles.remove(cell);
	}

	public void removeFromSelection(int i, int j) {
		Iterator<HexTile> it = selectedTiles.iterator();
		while (it.hasNext()) {
			HexTile p = it.next();
			if (p.getGeoX() == i && p.getGeoY() == j) {
				it.remove();
				return;
			}
		}
	}

	// The update method. to update.
	public void update(Graphics g) {
		super.paint(g);
		this.paint(g);
	}

	public void addAction(Action action) {
		mapActions.add(action);
	}


	public void paintVersion(Graphics graph, int imageWidth, int imageHeight) {
		String ver = "Created with jFactViewer 0.1";
		Graphics2D g = (Graphics2D) graph;
		Rectangle2D rect = g.getFont().getStringBounds("",
				g.getFontRenderContext());

		int x = (int) (getWidth() - rect.getWidth());
		int y = (int) (getHeight() - rect.getHeight());
		g.setColor(Color.WHITE);
		log.debug("Drawing version string at {}, {}", x, y);
		g.drawString(ver, x, y);
	}

	/**
	 * Returns an existing child node, or creates and returns a new child node
	 * (if the requested node does not exist).
	 * 
	 * @param rootNode
	 *            the <tt>IIOMetadataNode</tt> to search for the child node.
	 * @param nodeName
	 *            the name of the child node.
	 * @return the child node, if found or a new node created with the given
	 *         name.
	 */
	protected static IIOMetadataNode getNode(IIOMetadataNode rootNode,
			String nodeName) {
		int nNodes = rootNode.getLength();
		for (int i = 0; i < nNodes; i++) {
			if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0) {
				return ((IIOMetadataNode) rootNode.item(i));
			}
		}
		IIOMetadataNode node = new IIOMetadataNode(nodeName);
		rootNode.appendChild(node);
		return (node);
	}

	public HexTile getCell(Point p) {
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				if (tiles[i][j] != null && tiles[i][j].polygon.contains(p)) {
					return tiles[i][j];
				}
			}
		}
		return null;
	}

	public void addOverlay(Overlay o) {
		this.overlays.add(o);
		if (o instanceof MouseMotionListener) {
			this.addMouseMotionListener((MouseMotionListener) o);
		}
	}
	
	
	//------Getter and Setter----------------
	public int getNumberOfRows() {
		return rows;
	}

	public int getNumberOfColums(int row) {
		return cols;
	}

	public void setCell(int i, int j, HexTile cell) {
		this.tiles[i][j] = cell;
	}

	public HexTile getCell(int i, int j) {
		return tiles[i][j];
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(float minValue) {
		this.minValue = minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(float maxValue) {
		this.maxValue = maxValue;
	}

	@Override
	public HexTile addCell(int id, int i, int j) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HexTile getCellById(int id) {
		return cellBySoftId[id];
	}

	@Override
	public int getSelectedSlice() {
		return currentSlice;
	}

}
