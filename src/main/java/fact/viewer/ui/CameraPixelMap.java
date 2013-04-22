/**
 * 
 */
package fact.viewer.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.io.CsvStream;
import stream.io.SourceURL;
import fact.Constants;
import fact.FactViewer;
import fact.data.EventUtils;
import fact.image.Transformation;
import fact.image.overlays.Overlay;
import fact.viewer.SelectionListener;
import fact.viewer.colorMappings.ColorMapping;
import fact.viewer.colorMappings.NeutralColorMapping;

/**
 * @author chris
 * 
 */
public class CameraPixelMap extends HexMap implements MouseListener,
		MouseMotionListener {
	/** The unique class ID */
	private static final long serialVersionUID = 4003306850236739854L;

	static Logger log = LoggerFactory.getLogger(CameraPixelMap.class);
	final static SimpleDateFormat fmt = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss");
	final static DecimalFormat df = new DecimalFormat("000");

	HexTile cellBySoftId[] = new HexTile[1440];

	int currentSlice = 0;
	float colorOffset = 2049.0f;
	float colorScaler = 4096.0f;

	public float[][] vals = new float[1440][300];
	float[][] transformed = new float[1440][1];
	double[] average = new double[300];

	DefaultPixelMapping defaultPixelMapping = new DefaultPixelMapping();
	
	//WTF is that supposed to be ?
//	PixelMapping[] pixelMappings = new PixelMapping[] { defaultPixelMapping };

	int maxCellId = 0;
	int selectedCell = -1;

	Date date = new Date();
	Integer run = -1;
	String eventNum = "?";
	ColorMapping colorMap = new NeutralColorMapping();
	// Selected Overlay keys
	private Set<String> selectedOverlayKeys = null;

	// Sets whether Pixels on the map can be Selected
	private boolean isSelectable = true;

	Data event;

	private CellHighlighter cellHighlighter;

	private HexTile nCell;

	private HexTile oldCell;

	
	public float minValue;
	public float maxValue;

	private int roi;

	
	
	public CameraPixelMap(Double radius) {
		super(45, 41, radius);
		this.setBorderColor(Color.BLACK);

		for (int i = 0; i < vals.length; i++) {
			for (int j = 0; j < vals[i].length; j++) {
				vals[i][j] = 0.0f;
			}
		}

		for (int i = 0; i < vals[0].length; i++)
			average[i] = 0.0d;

		try {
			loadCells("fact-map.txt");
		} catch (Exception e) {
			log.error("Failed to load cell-mapping! Error: {}", e.getMessage());
			e.printStackTrace();
		}
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		cellHighlighter = new CellHighlighter(this);
		this.addOverlay(cellHighlighter);
		// this.setLayout(Border)
	}

	public void addSelectionListener(SelectionListener l) {
		selectionListener.add(l);
	}

	public void selectOriginal() {
		for (int i = 0; i < vals.length; i++) {
			getCell(i).setColor(colorMap.map(vals[i][currentSlice]));
		}
		repaint();
	}

	public void transform(Transformation tfn) {
		for (int i = 0; i < vals.length; i++) {
			getCell(i).setColor(colorMap.map(tfn.transform(vals[i])));
		}
		repaint();
	}

	public void loadCells(String map) throws Exception {

		int minX = 0;
		int maxX = 0;
		int minY = 0;
		int maxY = 0;

		URL mapping = CameraPixelMap.class.getResource("/" + map);
		log.debug("Loading pixel-mapping from {}", mapping);
		CsvStream stream = new CsvStream(new SourceURL(mapping), "\\s+");
		stream.init();
		Data item = stream.readNext();
		int i = 0;
		while (item != null) {
			i++;
			log.debug("{}", item);
			
//			int id = new Double((Double) item.get("softID")).intValue();
	//		int x = new Double((Double) item.get("geom_i")).intValue();
		//	int y = new Double((Double) item.get("geom_j")).intValue();
				int id = (int)(Double.parseDouble(item.get("softID").toString()));
				int x = (int)(Double.parseDouble(item.get("geom_i").toString()));
				int y = (int)(Double.parseDouble(item.get("geom_j").toString()));

			if (x < minX)
				minX = x;

			if (x > maxX)
				maxX = x;

			if (y < minY)
				minY = y;

			if (y > maxY)
				maxY = y;

			HexTile cell = addCell(id, x, y);
			cell.setId(id);
			item = stream.readNext();
		}

		log.debug(" x range is {}, {}", minX, maxX);
		log.debug(" y range is {}, {}", minY, maxY);
	}

	public HexTile addCell(int softId, int i, int j) {
		HexTile cell = new HexTile(i + 22, j + 19, getCellRadius());
		cell.setColor(Color.blue);
		setCell(i + 22, j + 19, cell);
		cellBySoftId[softId] = cell;
		maxCellId = Math.max(maxCellId, softId);

		cell.getInfo().put("SoftID", "" + softId);
		cell.getInfo().put("GeomX", i + "");
		cell.getInfo().put("GeomY", j + "");
		return cell;
	}

	public void selectSlice(int i) {
		log.debug("Selecting slice: {}", i);

		if (i >= 0 && i < vals[0].length) {
			currentSlice = i;
			for (int p = 0; p < vals.length; p++) {
				if (cellBySoftId[p] != null)
					cellBySoftId[p].setColor(colorMap.map(vals[p][i]));

				if (p == selectedCell && log.isDebugEnabled()) {
					log.debug("Value of cell {} is: {}", p, vals[p][i]);
					log.debug("   current offset is: {}", colorOffset);
					log.debug("   current scaler is: {}", colorScaler);
				}
			}
			this.repaint();
		}
	}


	/**
	 * This simply renews the list of overlays that can be drawn in the current
	 * event
	 * 
	 */
	private void updateOverlays() {
		overlays.clear();
		if (selectedOverlayKeys != null) {
			for (String key : selectedOverlayKeys) {
				Serializable value = event.get(key);
				if (value != null && value instanceof Overlay) {
					overlays.add((Overlay) value);
				}
			}
		}
	}

	public int getNumberOfSlices() {
		if (vals != null && vals.length > 0)
			return vals[0].length;
		return 0;
	}

	public double[] getSliceAverages() {
		return average;
	}

	public void saveAnimagedGif(final File image, final int start,
			final int end, final int stepping) {
		if (image != null) {
			log.info("Saving GIF in {}", image);

			try {

				Task task = new Task() {

					Double completed = 0.0d;

					@Override
					public String getStatus() {
						return "Creating animated gif...";
					}

					@Override
					public double percentageCompleted() {
						log.debug("Returning completion {}", completed);
						return completed;
					}

					@Override
					public void run() {

						try {

							BufferedImage buf = new BufferedImage(getWidth(),
									getHeight() + 8,
									BufferedImage.TYPE_INT_ARGB);
							ImageWriter gifWriter = (ImageWriter) ImageIO
									.getImageWritersBySuffix("GIF").next();
							ImageWriteParam imageWriteParam = gifWriter
									.getDefaultWriteParam();
							ImageTypeSpecifier imageTypeSpecifier = new ImageTypeSpecifier(
									buf);

							IIOMetadata imageMetaData = gifWriter
									.getDefaultImageMetadata(
											imageTypeSpecifier, imageWriteParam);
							String metaFormatName = imageMetaData
									.getNativeMetadataFormatName();

							IIOMetadataNode root = (IIOMetadataNode) imageMetaData
									.getAsTree(metaFormatName);
							IIOMetadataNode graphicsControlExtensionNode = getNode(
									root, "GraphicControlExtension");

							graphicsControlExtensionNode.setAttribute(
									"disposalMethod", "none");
							graphicsControlExtensionNode.setAttribute(
									"userInputFlag", "FALSE");
							graphicsControlExtensionNode.setAttribute(
									"transparentColorFlag", "FALSE");
							graphicsControlExtensionNode.setAttribute(
									"delayTime", Integer.toString(10));
							log.info("Delay time is 10");
							graphicsControlExtensionNode.setAttribute(
									"transparentColorIndex", "0");

							IIOMetadataNode commentsNode = getNode(root,
									"CommentExtensions");
							commentsNode.setAttribute("CommentExtension",
									"Created by MAH");

							IIOMetadataNode appEntensionsNode = getNode(root,
									"ApplicationExtensions");
							IIOMetadataNode child = new IIOMetadataNode(
									"ApplicationExtension");

							child.setAttribute("applicationID", "NETSCAPE");
							child.setAttribute("authenticationCode", "2.0");

							int loop = 0;
							child.setUserObject(new byte[] { 0x1,
									(byte) (loop & 0xFF),
									(byte) ((loop >> 8) & 0xFF) });
							appEntensionsNode.appendChild(child);

							imageMetaData.setFromTree(metaFormatName, root);

							ImageOutputStream ios = ImageIO
									.createImageOutputStream(new FileOutputStream(
											image));
							gifWriter.setOutput(ios);

							Graphics2D g = buf.createGraphics();
							gifWriter.prepareWriteSequence(null);

							// int slice = this.currentSlice;

							Double total = new Double(end);

							for (int i = start; i < end; i += stepping) {
								// Draw into the BufferedImage, and then do
								this.completed = 100.0 * ((new Double(i)) / total
										.doubleValue());
								this.advanced();

								log.info("Drawing slice {}", i);
								drawImage(g, buf.getWidth(), buf.getHeight(), i);
								gifWriter.writeToSequence(new IIOImage(buf,
										null, imageMetaData), imageWriteParam);
							}
							gifWriter.endWriteSequence();
							ios.flush();
							ios.close();
							// selectSlice( slice );

						} catch (Exception e) {

						}

						finish();
					}
				};

				ImageIcon icon = new ImageIcon(
						CameraPixelMap.class
								.getResource("/icons/generate-gif.png"));
				TaskProgressDialog d = new TaskProgressDialog(
						FactViewer.getInstance(), icon);
				task.addTaskMonitor(d);
				d.center(FactViewer.getInstance());
				task.start();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void drawImage(Graphics g, int width, int height, int slice) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);
		// System.out.println("Dimensions CamMap:  " + width + ", " + height);

		this.selectSlice(slice);
		// double[] values = getValuesInSlice( slice );
		// paintTiles( g, values, colorMap );
		this.paint(g);

		SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		DecimalFormat df = new DecimalFormat("000");
		String runString = "???";
		if (run > 0)
			runString = df.format(run);

		String dateString = "";
		if (date != null)
			dateString = fmt.format(date);

		int y = height - 10;
		int x = 10;
		g.setColor(Color.WHITE);
		g.setFont(g.getFont().deriveFont(9.0f));
		g.drawString(dateString + ", Run " + runString + ", Event " + eventNum
				+ ", Slice " + currentSlice, x, y);

		paintVersion(g, width, height);

	}

	public void setActiveOverlayKeys(Set<String> keySet) {

		// System.out.println("camMap notices change in checkboxes");
		selectedOverlayKeys = keySet;
		updateOverlays();
		for (Overlay o : overlays) {
			o.setCamMap(this);
			o.paint(this.getGraphics(), tiles);
		}
		repaint();
	}

	/**
	 * @see fact.viewer.ui.HexMap#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// super.mouseClicked(arg0);

		if (arg0.getButton() == MouseEvent.BUTTON1 && isSelectable) {

			for (HexTile cell : cellBySoftId) {
				if (cell.polygon.contains(arg0.getPoint())) {
					log.info("HexTile {} selected", cell.getId());

					selectedCell = cell.getId();
					boolean selected = isSelected(cell);
					boolean ctrlDown = arg0.isControlDown();
					log.info("Cell already selected? {}", selected);
					log.info("CTRL down? {}", ctrlDown);

					if (ctrlDown && selected) {
						log.info("Removing cell from selection");
						this.removeFromSelection(cell);
					} else {
						if (!arg0.isControlDown()) {
							this.clearSelection();
						}

						log.info("Selected cell at {}, {}", cell.getGeoX(),
								cell.getGeoY());
						//
						System.out.println("coords x: " + cell.getBounds().x
								+ " coords y: " + cell.getBounds().y);
						//
						addToSelection(cell);

						for (SelectionListener l : this.selectionListener) {
							log.info("Notifying selection-listener {}", l);
							l.itemSelected(this, getSelectedIds());
						}
					}

					log.info("Selected: {}", this.selectedTiles);
					// update all chartpanels
					for (ChartWindow cW : FactViewer.getInstance()
							.getChartWindowList()) {
						cW.setEvent(event, getSelectedIds());
					}

					for (EventInfoWindow evW : FactViewer.getInstance()
							.getEvWList()) {
						evW.setSelectedIds(getSelectedIds());
					}
					repaint();
					return;
				}
			}
		}

//		if (arg0.getButton() == MouseEvent.BUTTON3) {
//
//			JPopupMenu popup = new JPopupMenu();
//
//			Map<String, JPopupMenu> groups = new LinkedHashMap<String, JPopupMenu>();
//
//			for (Action action : mapActions) {
//
//				if (action instanceof SelectAction) {
//					SelectAction sa = (SelectAction) action;
//					JPopupMenu menu = groups.get(sa.getGroup());
//					if (menu == null) {
//						menu = new JPopupMenu(sa.getGroup());
//						groups.put(sa.getGroup(), menu);
//						popup.add(menu);
//						menu.add(sa);
//					}
//				} else {
//					popup.add(new JMenuItem(action));
//				}
//			}
//
//			popup.show(this, arg0.getX(), arg0.getY());
//		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		oldCell = nCell;
		nCell = getCell(arg0.getPoint());
		if (nCell != oldCell && nCell != null) {
			// this.x = cell.getGeoX();
			// this.y = cell.getGeoY();
			repaint();
			cellHighlighter.highlight(nCell.getGeoX(), nCell.getGeoY());
			// log.debug("Cell is at {}, {}", x, y);
			cellHighlighter.paint(getGraphics(), tiles);
			if (arg0.isShiftDown()) {
				FactViewer.getInstance().getCamMap().addToSelection(nCell);
				for (ChartWindow cW : FactViewer.getInstance()
						.getChartWindowList()) {
					cW.setEvent(event, getSelectedIds());
				}

				for (EventInfoWindow evW : FactViewer.getInstance()
						.getEvWList()) {
					evW.setSelectedIds(getSelectedIds());
				}
			}

			// NewUI.getInstance().setPixelInfo(cell.getInfo());
		} else {
			cellHighlighter.paint(getGraphics(), tiles);
		}

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * Getter and Setter start here
	 */
	public ColorMapping getColorMapping() {
		return colorMap;
	}
	public void setColorMapping(ColorMapping c){
		this.colorMap = c;
		this.selectSlice(currentSlice);
	}
	
	public boolean isSelectable() {
		return isSelectable;
	}

	public void setSelectable(boolean isSelectable) {
		this.isSelectable = isSelectable;
	}
	
	public int getRoi() {
		return roi;
	}
	public void setRoi(int roi) {
		this.roi = roi;
	}
	
	
	
	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}
	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	
	
	/**
	 * @return the run
	 */
	public Integer getRun() {
		return run;
	}
	/**
	 * @param run
	 *            the run to set
	 */
	public void setRun(Integer run) {
		this.run = run;
	}
	
	
	
	public HexTile getCell(Point p) {
		for (HexTile cell : cellBySoftId) {
			if (cell.polygon.contains(p)) {
				return cell;
			}
		}
		return null;
	}
	public HexTile[] getCells() {
		return cellBySoftId;
	}
	
	
	
	public HexTile getCell(int softId) {
		return cellBySoftId[softId];
	}

	public int getMaxCellId() {
		return maxCellId;
	}

	/**
	 * @see fact.viewer.ui.HexMap#setCell(int, int, fact.viewer.ui.HexTile)
	 */
	@Override
	public void setCell(int i, int j, HexTile cell) {
		super.setCell(i, j, cell);
		cell.setBorderColor(Color.white);
	}
	
	
	
	
	public void setData(double[] ds) {
		setData(EventUtils.doubleToFloatArray(ds));
	}

	public void setData(float[] slices) {
		if (slices == null) {
			log.error("No data found in event!");
			return;
		}

		// i want to keep pixels selected
		// clearSelection();

		minValue = Float.MAX_VALUE;
		maxValue = Float.MIN_VALUE;

		roi = slices.length / Constants.NUMBEROFPIXEL;
		average = new double[roi];
		vals = new float[Constants.NUMBEROFPIXEL][roi];
		// float[] calibrated = (float[]) event.get( "DataCalibrated" );
		// if( calibrated != null )
		// slices = calibrated;
		float[][] values = defaultPixelMapping.sortPixels(slices);

		// if( calibrated != null ){
		// vals = defaultPixelMapping.sortPixels( calibrated, offsets );
		// }

		for (int row = 0; row < vals.length && row < values.length; row++) {
			for (int s = 0; s < vals[row].length && s < values[row].length; s++) {
				vals[row][s] = values[row][s];
				if (!Float.isNaN(vals[row][s])) {
					minValue = Math.min(minValue, vals[row][s]);
					maxValue = Math.max(maxValue, vals[row][s]);
				}
				
				//this is somewhat weird. average in the first slice will be the value of the first slice in the last row.
//				if (s == 0) {
//					average[s] = vals[row][s];
//				} else {
//					average[s] += vals[row][s];
//				}
				
				//this looks better
				average[s] += vals[row][s];
			}
		}

		log.debug("minimum is: {}, maximum is: {}", minValue, maxValue);
		(colorMap).setMinMax(minValue, maxValue);
		for (int i = 0; i < average.length; i++) {
			average[i] = average[i] / vals.length;
		}

		log.debug("Slices loaded.");
		selectSlice(currentSlice);
		repaint();
	}

	public void setEvent(Data item) {
		if (item != null) {
			event = item;
			this.eventNum = event.get("EventNum") + "";
			// float[] slices = (float[]) event.get("Data");
			updateOverlays();
			for (Overlay o : overlays) {
				o.setCamMap(this);
				o.paint(this.getGraphics(), tiles);
			}
			this.repaint();
		}
	}
	
}