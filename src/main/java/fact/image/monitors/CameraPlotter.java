package fact.image.monitors;

import java.awt.BorderLayout;
import java.io.Serializable;
import java.util.LinkedHashMap;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.annotations.Parameter;
import stream.plotter.DataVisualizer;
import fact.Constants;
import fact.data.EventUtils;
import fact.image.OnlineStatistics;
import fact.viewer.ui.MapView;

public class CameraPlotter extends DataVisualizer  {
	static Logger log = LoggerFactory.getLogger(AverageBarPlotter.class);
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

	private String[] keys;
	public String[] getKeys() {
		return keys;
	}
	@Parameter(required = false, description = "The attributes/features to be plotted (non-numerical features will be ignored)")
	public void setKeys(String[] keys) {
		this.keys = keys;
	}
	
	
	private boolean drawErrors = false;
	public boolean isDrawErrors() {
		return drawErrors;
	}
	@Parameter(required = true, description = "Flag to toggle drawing of Errorbars in plot.")
	public void setDrawErrors(boolean drawErrors) {
		this.drawErrors = drawErrors;
	}


	private Double panelSize = 6.0;
	public Double getPanelSize() {
		return panelSize;
	}
	@Parameter(required = true, description = "Size of the camera Pixel being drawn", defaultValue="6.0")
	public void setPanelSize(Double panelSize) {
		this.panelSize = panelSize;
	}

	private boolean showAverage = false;
	public boolean isShowAverage() {
		return showAverage;
	}
	@Parameter(required = true, description = "Toggles wether the value of the key or the average of the values of the key should be drawn")
	public void setShowAverage(boolean showAverage) {
		this.showAverage = showAverage;
	}

	private OnlineStatistics onStat;
	private MapView mapView;
	
	public CameraPlotter(){
		
	}
	
	@Override
	public void init(ProcessContext ctx) throws Exception{
		super.init(ctx);
		mapView = new MapView(null, false, false);
		if(showAverage){
			onStat = new OnlineStatistics();
		}
		frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(mapView, BorderLayout.CENTER);
		frame.setSize(mapView.getPreferredSize());
		frame.setVisible(true);
	}
	
	@Override
	public Data processMatchingData(Data data) {
		for(String key: getKeys()){
			//check wether data contains the key, item is of the right type and array has the right length to be plotted  into camera picture
			if(!data.containsKey(key)){
				//key doesnt exist in map
				log.info(Constants.ERROR_WRONG_KEY + key + ",  " + this.getClass().getSimpleName() );
				return data;
			}
			if (data.get(key).getClass().isArray())
			{
				Serializable val = data.get(key);
				Class<?> clazz = val.getClass().getComponentType();
				if(clazz.equals(float.class)||clazz.equals(double.class)||clazz.equals(int.class)){
					float[] valArray = EventUtils.toFloatArray(val);
					if(valArray.length == Constants.NUMBEROFPIXEL){
						if(showAverage){
							onStat.updateValues(key, EventUtils.toDoubleArray(valArray));
							mapView.setData(EventUtils.toFloatArray(onStat.getValueMap().get(key+"_avg")));
						}
						else
						{
							mapView.setData(valArray);
						}
					}
				}
				
			}
			else
			{
				log.info("This data cannot be displayed in a camera window");
			}
		}
		return data;
	}
	
}
