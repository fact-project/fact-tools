package fact.image.monitors;

import java.awt.BorderLayout;
import java.io.Serializable;
import java.util.LinkedHashMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.annotations.Parameter;
import stream.plotter.DataVisualizer;
import fact.Constants;
import fact.viewer.ui.MapView;

public class ShowerCameraPlotter extends DataVisualizer  {
	static Logger log = LoggerFactory.getLogger(HistogramPlotter.class);
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

	private String key = "DataCalibrated";
	public String getKey() {
		return key;
	}
	@Parameter(required = false, description = "The attribute/feature to be plotted (non-numerical features will be ignored)")
	public void setKey(String key) {
		this.key = key;
	}

	private String showerKey = " ";
	public String getShowerKey() {
		return showerKey;
	}
	public void setShowerKey(String showerKey) {
		this.showerKey = showerKey;
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
	private int[] corePixels;
	
	public ShowerCameraPlotter(){
		
	}
	
	@Override
	public void init(ProcessContext ctx) throws Exception{
		super.init(ctx);
		mapView = new MapView(null, true, false);
		mapView.setKey(key);
		frame = new JFrame();
		frame.setSize(mapView.getPreferredSize());
		frame.setSize(frame.getWidth(), frame.getHeight()+25);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(mapView, BorderLayout.CENTER);
		
		JPanel statusPanel = new JPanel();
//		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusPanel.setSize(frame.getWidth(), 10);
		JLabel label = new JLabel("ShowerKey:  " + showerKey);
		label.setSize(100, 10);
		statusPanel.add(label);
		frame.add(statusPanel, BorderLayout.SOUTH);
		frame.setVisible(true);
	}
	
	@Override
	public Data processMatchingData(Data data) {
		if(data.containsKey(showerKey)){
			Serializable val = data.get(showerKey);
			if (val.getClass().isArray()) {
				if (val.getClass().getComponentType() == int.class) {
					corePixels = (int[])val;
				}
			}
		} else {
			log.info(Constants.ERROR_WRONG_KEY + showerKey + ",  " + this.getClass().getSimpleName() );
		}
		
		mapView.setShowerChids(corePixels);
		mapView.setEvent(data);
		return data;
	}
	
}
