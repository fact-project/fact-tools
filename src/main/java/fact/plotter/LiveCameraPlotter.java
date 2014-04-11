package fact.plotter;

import java.awt.BorderLayout;
import java.util.LinkedHashMap;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.annotations.Parameter;
import stream.plotter.DataVisualizer;
import fact.viewer.ui.MapView;

public class LiveCameraPlotter extends DataVisualizer  {
	static Logger log = LoggerFactory.getLogger(LiveCameraPlotter.class);
	JFrame frame;
	LinkedHashMap<String, double[]> valueMap = new LinkedHashMap<String , double[]>();
	//this map contains counters that count the number of vales that have arrived for every key
	LinkedHashMap<String, Integer> counterMap = new LinkedHashMap<String, Integer>();
	private boolean keepOpen = true;
	public boolean isKeepOpen() {
		return keepOpen;
	}
	@Parameter(required = true, description = "Flag indicates wther the window stays open after the process has finished", defaultValue = "true")
	public void setKeepOpen(boolean keepOpen) {
		this.keepOpen = keepOpen;
	}

	private String key;
	public String getKey() {
		return key;
	}
	@Parameter(required = false, description = "The attributes/features to be plotted (non-numerical features will be ignored)")
	public void setKey(String key) {
		this.key = key;
	}

	private Double panelSize = 6.0;
	public Double getPanelSize() {
		return panelSize;
	}
	@Parameter(required = true, description = "Size of the camera Pixel being drawn", defaultValue="6.0")
	public void setPanelSize(Double panelSize) {
		this.panelSize = panelSize;
	}

	private MapView mapView;
	

	@Override
	public void init(ProcessContext ctx) throws Exception{
		super.init(ctx);
		mapView = new MapView(null, true, false, 6.0d);
		mapView.setKey(key);
		frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(mapView, BorderLayout.CENTER);
		frame.setSize(mapView.getPreferredSize());
		frame.setVisible(true);
	}
	
	@Override
	public Data processMatchingData(Data data) {
		mapView.setEvent(data);
		return data;
	}
	
}
