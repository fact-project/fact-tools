/**
 * 
 */
package fact;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.io.AbstractStream;
import stream.io.SourceURL;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import fact.image.Pixel;
import fact.image.overlays.PixelSet;
import fact.io.FitsStream;
import fact.io.SerializedEventStream;
import fact.viewer.actions.ChangeColorMap;
import fact.viewer.ui.CamWindow;
import fact.viewer.ui.CameraPixelMap;
import fact.viewer.ui.ChartWindow;
import fact.viewer.ui.EventInfoWindow;
import fact.viewer.ui.NavigationPanel;
import fact.viewer.ui.OverlayPanel;
import fact.viewer.ui.ScalePanel;
import fact.viewer.ui.SimplePlotPanel;

/**
 * @author kai
 * 
 */
public class FactViewer extends JFrame {

	/** The unique class ID */
	private static final long serialVersionUID = -5687227971590846044L;
	static Logger log = LoggerFactory.getLogger(FactViewer.class);

	private static FactViewer viewer = null;
	final NavigationPanel navigation = new NavigationPanel();

	// this string is never really used as far as i can tell;

	String source = null;
	Data event;
	AbstractStream stream;
	Integer eventNumber = 0;
	File calibrationFile = null;

	// key for the Data currently displayed in the mainwindow. This should
	// usually be either Data or DataCalibratred
	private String currentKey = "";
	// DrsCalibration drsCalibration = new DrsCalibration();

	// ProcessorList preprocessing = new ProcessorList();
	Set<Integer> selectedPixel;
	private CameraPixelMap camMap;

	public CameraPixelMap getCamMap() {
		return camMap;
	}

	public void setCamMap(CameraPixelMap camMap) {
		this.camMap = camMap;
	}

	private SimplePlotPanel chartPanel;
	private ArrayList<CamWindow> camWindowList = new ArrayList<CamWindow>();
	private ArrayList<ChartWindow> chartWindowList = new ArrayList<ChartWindow>();
	private ArrayList<EventInfoWindow> evWList = new ArrayList<EventInfoWindow>();
	private OverlayPanel over;
	private ScalePanel scale;

	public OverlayPanel getOverlayPanel() {
		return over;
	}

	public void setOverPanel(OverlayPanel over) {
		this.over = over;
	}

	public ArrayList<ChartWindow> getChartWindowList() {
		return chartWindowList;
	}

	public void setChartWindowList(ArrayList<ChartWindow> chartWindowList) {
		this.chartWindowList = chartWindowList;
	}

	public static FactViewer getInstance() {

		if (viewer == null) {
			viewer = new FactViewer();
		}

		return viewer;
	}

	private FactViewer() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Fact Tool");
		// eventPanel = new FactViewerPanel();
		JMenuBar menu = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenuItem open = new JMenuItem("Open file");
		open.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser f = new JFileChooser();
				int ret = f.showOpenDialog(null);
				if (ret == JFileChooser.APPROVE_OPTION) {
					try {
						loadFitsFile(f.getSelectedFile());
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null,
								"Error: " + e.getLocalizedMessage());
					}
				}
			}
		});
		file.add(open);

		open = new JMenuItem("Quit");
		open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		file.add(open);
		menu.add(file);
//		
//		JMenu exportMenu = new JMenu("Export");
//		
//		JMenuItem exportGif =  new JMenuItem("Export as animated .gif File");
//		exportGif.addActionListener(new ExportAnimatedGifAction(this));
//		exportMenu.add(exportGif);
//		
//		JMenuItem exportPNG =  new JMenuItem("Export as .png File");
//		exportGif.addActionListener(new ExportPNGAction(this.getCamMap()));
//		exportMenu.add(exportPNG);
//		
//		
//		menu.add(exportMenu);

		JMenu select = new JMenu("Selection");
		JMenuItem invert = new JMenuItem("Invert");
		invert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				CameraPixelMap map = camMap;
				Set<Integer> selected = map.getSelectedIds();
				Set<Integer> newSelection = new HashSet<Integer>();
				for (Integer i = 0; i < 1440; i++) {
					if (!selected.contains(i)) {
						newSelection.add(i);
					}
				}
				map.clearSelection();
				map.setSelectedIds(newSelection);
			}
		});
		select.add(invert);

		JMenuItem asShower = new JMenuItem("Annotate as shower");
		asShower.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				CameraPixelMap map = camMap;
				Set<Integer> ids = map.getSelectedIds();
				PixelSet shower = new PixelSet();
				for (Integer id : ids) {
					shower.add(new Pixel(id));
				}
				event.put("@shower", shower);
				// overlays.set(event);
				// overlays.revalidate();
			}
		});
		select.add(asShower);
		menu.add(select);
		this.setJMenuBar(menu);

		JMenu mnWindows = new JMenu("Windows");
		menu.add(mnWindows);

		/**
		 * Button for a new camera Window. Action listener creates the window
		 * and sets the currently selected slice.
		 */
		JMenuItem mntmNewCameraWindow = new JMenuItem("New Camera Window");
		mntmNewCameraWindow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CamWindow camW = new CamWindow(FactViewer.this);
				camW.setEvent(event);
				camW.setSlice(navigation.getSliceSlider().getValue());
				camWindowList.add(camW);

			}
		});
		mnWindows.add(mntmNewCameraWindow);

		JMenuItem mntmNewGraphWindow = new JMenuItem("New Graph Window");
		mntmNewGraphWindow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChartWindow chartW = new ChartWindow(FactViewer.this);
				chartW.setEvent(event, camMap.getSelectedIds());
				chartW.setSlice(navigation.getSliceSlider().getValue());
				chartWindowList.add(chartW);

			}
		});
		mnWindows.add(mntmNewGraphWindow);

		JMenuItem mntmNewEventInfoWindow = new JMenuItem("Event Info Window");
		mntmNewEventInfoWindow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// ChartWindow chartW = new ChartWindow(NewUI.this);
				// chartW.setEvent(event, camMap.getSelectedIds());
				// chartW.setSlice(navigation.getSliceSlider().getValue());
				// chartWindowList.add(chartW);
				EventInfoWindow evW = new EventInfoWindow(FactViewer.this);
				evW.setEvent(event);
				evWList.add(evW);

			}
		});
		mnWindows.add(mntmNewEventInfoWindow);

		// JCheckBoxMenuItem chckbxmntmShowEventInformation = new
		// JCheckBoxMenuItem("Show Event Information");
		// mnWindows.add(chckbxmntmShowEventInformation);

		JCheckBoxMenuItem chckbxmntmShoweditComments = new JCheckBoxMenuItem(
				"Show/Edit Comments");
		mnWindows.add(chckbxmntmShoweditComments);

		navigation.getNextButton().setEnabled(stream != null);
		navigation.getNextButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadNextEvent();
			}
		});

		navigation.getSliceSlider().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				int slice = navigation.getSliceSlider().getValue();
				navigation.getSliceField().setText("" + slice);
				camMap.setCurrentSlice(slice);
				for (CamWindow c : camWindowList) {
					c.setSlice(slice);
				}

				chartPanel.setSlice(slice);
				for (ChartWindow cW : chartWindowList) {
					cW.setSlice(slice);
				}
				// chartPanel.getMarker().setValue(slice);
			}
		});

		navigation.getSliceField().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				log.info("ActionEvent: {}", arg0);
				try {
					Integer slice = new Integer(navigation.getSliceField()
							.getText());
					selectSlice(slice);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});

		//
		//
		getContentPane().setLayout(
				new FormLayout(new ColumnSpec[] {
						ColumnSpec.decode("left:max(300px;pref):grow"),
						ColumnSpec.decode("default"),
						ColumnSpec.decode("right:max(400px;pref):grow"), },
						new RowSpec[] { RowSpec.decode("top:0px"),
								RowSpec.decode("pref:grow"),
								RowSpec.decode("default"),
								RowSpec.decode("default"), }));

		camMap = new CameraPixelMap(7.0d);
		JPanel mapPanel = new JPanel(new BorderLayout());
		mapPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null,
				null, null));
		camMap.setBackground(Color.BLACK);
		mapPanel.add(camMap, BorderLayout.CENTER);
		scale = new ScalePanel(camMap.getColorMapping(), -10, 700);
		scale.setBackground(camMap.getBackground());
		mapPanel.add(scale, BorderLayout.EAST);
		getContentPane().add(mapPanel, "1, 2, default, top");
		JPopupMenu popupMenu = new JPopupMenu("Title");
//	    PopupMenuListener popupMenuListener = new MyPopupMenuListener();

//	    popupMenu.addPopupMenuListener(popupMenuListener);
	
		ChangeColorMap cmAction = new ChangeColorMap(camMap, scale);
		JMenuItem colorMapMenuItem1 = new JMenuItem("TwoToneAbsolute");
	    colorMapMenuItem1.addActionListener(cmAction);
	    popupMenu.add(colorMapMenuItem1);

	    JMenuItem colorMapMenuItem2 = new JMenuItem("NeutralColor");
	    popupMenu.add(colorMapMenuItem2);
	    colorMapMenuItem2.addActionListener(cmAction);
	    
	    JMenuItem colorMapMenuItem3 = new JMenuItem("GrayScale");
	    popupMenu.add(colorMapMenuItem3);
	    colorMapMenuItem3.addActionListener(cmAction);
	    
	    
		camMap.setComponentPopupMenu(popupMenu);

		chartPanel = new SimplePlotPanel();
		chartPanel.setBackground(Color.WHITE);
		chartPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null,
				null, null, null));
		chartPanel.setPreferredSize(new Dimension(550, 400));
		getContentPane().add(chartPanel, "3, 2, left, top");
		over = new OverlayPanel(this);
		getContentPane().add(over, "1, 3, 3, 1, left, top");
		// camMap.addOverlay(over);

		FlowLayout flowLayout = (FlowLayout) navigation.getLayout();
		flowLayout.setVgap(2);
		getContentPane().add(navigation, "1, 4, 3, 1, left, top");
		pack();
		setSize(1200, 640);
		// getContentPane().setLayout(new FormLayout(new ColumnSpec[] {},
		// new RowSpec[] {}));
	}

	/**
	 * @return the map
	 */
	// public MapView getMap() {
	// }

	public JButton getNextButton() {
		return navigation.getNextButton();
	}

	public JButton getPrevButton() {
		return navigation.getPrevButton();
	}

	//
	// public JLabel getPixelInfoLabel() {
	// return pixelInfo;
	// }

	public void loadFitsFile(File file) throws Exception {

		if (file.getName().endsWith(".event")
				|| file.getName().endsWith(".event.gz")) {
			stream = new SerializedEventStream(file);
		} else
			stream = new FitsStream(new SourceURL(file.toURI().toURL()));

		navigation.getNextButton().setEnabled(stream != null);
		navigation.setFile(file);
		eventNumber = 0;
		stream.init();
		loadNextEvent();
	}

	public void loadNextEvent() {
		try {
			if (stream == null)
				return;

			Data event = stream.readNext();

			// event = cut.process( event );
			// event = preprocessing.process(event);

			// ClusterPixels clusterer = new ClusterPixels();
			// event = clusterer.process( event );

			if (event != null) {
				eventNumber++;
				setEvent(event);
				log.info("EventNumber: {}", event.get("eventNum"));
				navigation.setEventNumber(eventNumber);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This will be called whenever a new Event is supposed to be displayed.
	 * 
	 * @param event
	 *            The Event to be displayed
	 */

	public void setEvent(Data event) {
		if (event != null) {
			this.event = event;

			try {
				navigation.setFile(new File(event.get("@source") + ""));
				navigation.setEventNumber(new Integer(event.get("EventNum")
						+ ""));
			} catch (Exception e) {
				log.error("Error: {}", e.getMessage());
			}
			log.debug("Pre-selected source: {}", this.source);

			// clear the chartpanel
			chartPanel.clearSeries();
			// draw on hexmap. either calibrated or default data;
			int roi = 300;
			if (event.keySet().contains(Constants.DEFAULT_KEY_CALIBRATED)) {

				camMap.setData((float[]) event
						.get(Constants.DEFAULT_KEY_CALIBRATED));
				roi = ((float[]) event.get(Constants.DEFAULT_KEY_CALIBRATED)).length
						/ Constants.NUMBEROFPIXEL;
				// also add average of all pixels to chartpanel
				chartPanel.addSeries("Avg-" + Constants.DEFAULT_KEY_CALIBRATED,
						camMap.getSliceAverages());

			} else if (event.keySet().contains(Constants.DEFAULT_KEY)) {
				camMap.setData((float[]) event.get(Constants.DEFAULT_KEY));
				roi = ((float[]) event.get(Constants.DEFAULT_KEY)).length
						/ Constants.NUMBEROFPIXEL;
				// also add average of all pixels to chartpanel
				chartPanel.addSeries("Avg-" + Constants.DEFAULT_KEY,
						camMap.getSliceAverages());
			} else if (event.keySet().contains(Constants.DEFAULT_KEY_MC)) {
				camMap.setData((float[]) event.get(Constants.DEFAULT_KEY_MC));
				roi = ((float[]) event.get(Constants.DEFAULT_KEY_MC)).length
						/ Constants.NUMBEROFPIXEL;
				chartPanel.addSeries("Avg-" + Constants.DEFAULT_KEY_MC,
						camMap.getSliceAverages());
			} else if (event.keySet().contains(
					Constants.DEFAULT_KEY_MC_CALIBRATED)) {
				camMap.setData((float[]) event
						.get(Constants.DEFAULT_KEY_MC_CALIBRATED));
				roi = ((float[]) event.get(Constants.DEFAULT_KEY_MC_CALIBRATED)).length
						/ Constants.NUMBEROFPIXEL;
				chartPanel.addSeries("Avg-"
						+ Constants.DEFAULT_KEY_MC_CALIBRATED,
						camMap.getSliceAverages());
			}
			navigation.setRoi(roi);
			over.set(event);
			over.revalidate();
			camMap.setEvent(event);
			
			

			for (CamWindow c : camWindowList) {
				c.setEvent(event);
			}
			for (ChartWindow c : chartWindowList) {
				c.setEvent(event, camMap.getSelectedIds());
			}
			for (EventInfoWindow evW : evWList) {
				evW.setEvent(event);
			}
			
			scale.setMax(camMap.getMaxValue());
			scale.setMin(camMap.getMinValue());
			scale.repaint();
			// sourceSelection.setEvent(event); //sourcelist
			// overlays.set(event); //overlays
			// eventInfo.setEvent(event); //eventinfo window
		}
	}

	public Data getEvent() {
		return event;
	}

	public void selectSlice(int i) {
		camMap.setCurrentSlice(i);
		navigation.getSliceField().setText("" + i);
		navigation.getSliceSlider().setValue(i);
	}

	// public void setCalibrationFile(File file) {
	// drsCalibration.setDrsFile(file.getAbsolutePath());
	// }

	/**
	 * @return the overlays
	 */
	// public OverlayPanel getOverlayPanel() {
	// return overlays;
	// }

	public ArrayList<CamWindow> getCamWindowList() {
		return camWindowList;
	}

	public void setCamWindowList(ArrayList<CamWindow> camWindowList) {
		this.camWindowList = camWindowList;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		FactViewer viewer = FactViewer.getInstance();
		// Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		viewer.setMinimumSize(new Dimension(900, 700));
		// viewer.setSize((int)(screenSize.width * 0.5f),
		// (int)(screenSize.height *0.5f) );
		File file = null;
		if (args.length > 0) {
			file = new File(args[0]);
		}

		if (args.length > 1) {
			File f = new File(args[1]);
			if (f.canRead()) {
				// viewer.setCalibrationFile(f);
			}
		}

		if (file != null && file.canRead()) {
			viewer.loadFitsFile(file);
		}
		viewer.setVisible(true);
	}

	public String getCurrentKey() {
		return currentKey;
	}

	public void setCurrentKey(String currentKey) {
		this.currentKey = currentKey;
	}

	public ArrayList<EventInfoWindow> getEvWList() {
		return evWList;
	}

	public void setEvWList(ArrayList<EventInfoWindow> evWList) {
		this.evWList = evWList;
	}

}