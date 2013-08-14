package fact.viewer.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;

import javax.swing.JFrame;

import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.ui.Layer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import fact.Constants;
import fact.FactViewer;

public class ChartWindow {

	short selectIndex = 0;
	private int slice = 0;
	// private CameraPixelMap camMap;
	Data event;
	FactViewer mainWindow;
	private SimplePlotPanel plotPanel;
	private SourceSelector sL;
	private Set<Integer> selectedPixelSet;
	static Logger log = LoggerFactory.getLogger(ChartWindow.class);

	/**
	 * @param m
	 */
	public ChartWindow(FactViewer m) {
		mainWindow = m;
		/**
		 * Add cameraPixelMap, scalePanel and set some Layout stuff:
		 */
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("pref:grow"),
				ColumnSpec.decode("pref:grow"),},
				new RowSpec[] {
				RowSpec.decode("max(445px;min):grow"),}));

		// frame.setResizable(false);

		plotPanel = new SimplePlotPanel();
		plotPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		plotPanel.setBorder(null);
		frame.getContentPane().add(plotPanel, "1, 1, fill, fill");
		sL = new SourceSelector(event, this);
		sL.setAlignmentX(Component.CENTER_ALIGNMENT);
		sL.setAlignmentY(Component.TOP_ALIGNMENT);
		sL.setPreferredSize(new Dimension(170,
				plotPanel.getPreferredSize().height));
		frame.getContentPane().add(sL, "2, 1, fill, fill");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				mainWindow.getChartWindowList().remove(this);
			}
		});
		frame.pack();
		frame.setVisible(true);

	}

	public void setEvent(Data event, Set<Integer> set) {
		this.event = event;
		sL.setEvent(event);
		updateGraph(event, set);
	}

	// just redraw some shiiiit
	public void updateGraph() {
		updateGraph(event, selectedPixelSet);
	}

	/**
	 * @param event
	 * @param selectedPixelSet
	 */
	public void updateGraph(Data event, Set<Integer> setP) {
		selectedPixelSet = setP;
		if (event != null) {
			plotPanel.clearSeries();
			plotPanel.getPlot().clearDomainMarkers();
			for (String key : sL.getSelectedKeys()) {
				try{
					IntervalMarker[] m = (IntervalMarker[]) event.get(key);
					addMarkerToPlot(event, selectedPixelSet, key, m);
				} catch(ClassCastException  e){
					addSeriesToPlot(event, selectedPixelSet, key, null);
				}
			}
			plotPanel.setSlice(slice);
		}
	}
	
	private void addMarkerToPlot(Data event, Set<Integer> set,
			String key, IntervalMarker[] m) {

		if (!set.isEmpty()) {
			for (int id : set) {
				id = DefaultPixelMapping.getChidID(id);
				if(m != null && m[id] != null){
					plotPanel.getPlot().addDomainMarker(m[id], Layer.BACKGROUND);
				}
			}
		} else {
			int id = 0;
			if(m != null && m[id] != null){
				plotPanel.getPlot().addDomainMarker(m[id], Layer.BACKGROUND);
			}
		}
		
	}

	/**
	 * @param event
	 * @param set
	 */
	private void addSeriesToPlot(Data event, Set<Integer> set, String key, IntervalMarker[] m) {
		Color color = getMatchingColorFromKey(key, event);
		if (!set.isEmpty()) {
			for (int id : set) {
				id = DefaultPixelMapping.getChidID(id);
				float[] data = (float[]) event.get(key);
				int roi = data.length / Constants.NUMBEROFPIXEL;
				plotPanel.addSeries(key + "-" + id, color, data, roi * id, (roi * id)
						+ roi);
			}
		} else {
			int id = 0;
			float[] data = (float[]) event.get(key);
			int roi = data.length / Constants.NUMBEROFPIXEL;
			plotPanel.addSeries(key + "-" + id, color, data, roi * id, (roi * id)
					+ roi);
		}
	}
	
	public Color getMatchingColorFromKey(String key, Data event) {
//		Data event = FactViewer.getInstance().getEvent();
		// remove pixelnumber from name
		key = key.replaceAll("-(\\d){1,4}", "");
		String colorKey = "@" + Constants.KEY_COLOR + "_" + key;

		if (event.containsKey(colorKey)) {
			try{
				return Color.decode((String) event.get(colorKey));
			} catch (NumberFormatException e){
				log.error("Could not parse the Color String. " +  (String) event.get(colorKey) + " is not a valid color String of the form \" #HHHHHH \". " );
			}
		}

		return null;
	}

	public void setSlice(int i) {
		slice = i;
//		plotPanel.setSlice(slice);
		plotPanel.getPlot().clearDomainMarkers(2);
		plotPanel.getPlot().addDomainMarker(2, new ValueMarker(i), Layer.FOREGROUND);
	}


	public Set<Integer> getSet() {
		return selectedPixelSet;
	}

	public void setSet(Set<Integer> set) {
		this.selectedPixelSet = set;
	}

	public SimplePlotPanel getPlotPanel() {
		return plotPanel;
	}

	public void setPlotPanel(SimplePlotPanel plotPanel) {
		this.plotPanel = plotPanel;
	}

}
