package fact.plotter;

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
import fact.EventUtils;
import fact.image.OnlineStatistics;
import fact.viewer.ui.MapView;
/**
 * Plot a view of the given array in the camera. The mean values will be plotted. This way you can easily check how a parameter ist distributed over the geometry of the camera.
 *  
 * @author bruegge
 *
 */
public class CameraPlotter extends DataVisualizer  {
	static Logger log = LoggerFactory.getLogger(BarPlotter.class);
	JFrame frame;
	LinkedHashMap<String, double[]> valueMap = new LinkedHashMap<String , double[]>();
	//this map contains counters that count the number of vales that have arrived for every key
	LinkedHashMap<String, Integer> counterMap = new LinkedHashMap<String, Integer>();
	
	@Parameter(required = true)
	private String key;

    @Parameter
    private boolean keepOpen = true;
    @Parameter(description = "Size of the camerapixels in the plot", defaultValue = "6.0")
    private Double pixelSize = 6.0;

    @Parameter(description = "Title of the plot")
	private String title ="Default Title";

	private OnlineStatistics onStat;
	private MapView mapView;
	
	
	@Override
	public void init(ProcessContext ctx) throws Exception{
		super.init(ctx);
		mapView = new MapView(null, false, false, pixelSize);
		onStat = new OnlineStatistics();
		frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(mapView, BorderLayout.CENTER);
		frame.setSize(mapView.getPreferredSize());
		frame.setTitle(title);
		frame.setVisible(true);
	}
	
	@Override
	public Data processMatchingData(Data data) {
			//check wether data contains the key, item is of the right type and array has the right length to be plotted  into camera picture
			if(!data.containsKey(key)){
				//key doesnt exist in map
				log.error("Key not found " + key + ",  " + this.getClass().getSimpleName() );
				throw new RuntimeException("The key " + key + " does not exist in the item");
			}
			if (data.get(key).getClass().isArray())
			{
				Serializable val = data.get(key);
				Class<?> clazz = val.getClass().getComponentType();
				if(clazz.equals(float.class)||clazz.equals(double.class)||clazz.equals(int.class)){
					double[] valArray = EventUtils.toDoubleArray(val);
					if(valArray.length == Constants.NUMBEROFPIXEL){
							onStat.updateValues(key, EventUtils.toDoubleArray(valArray));
							mapView.setData(EventUtils.toDoubleArray(onStat.getValueMap().get(key+"_avg")));
					}
				}
			}
			else
			{
				log.info("This data cannot be displayed in a camera window cause its not an array");
				throw new RuntimeException("The key " + key + " does not refer to an array");
			}
		return data;
	}
	
	public boolean isKeepOpen() {
		return keepOpen;
	}
	@Parameter(required = true, description = "Flag indicates wther the window stays open after the process has finished", defaultValue = "true")
	public void setKeepOpen(boolean keepOpen) {
		this.keepOpen = keepOpen;
	}


	public Double getPixelSize() {
		return pixelSize;
	}
	@Parameter(required = false, description = "Size of the camera Pixel being drawn", defaultValue="6.0")
	public void setPixelSize(Double panelSize) {
		this.pixelSize = panelSize;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getTitle() {
		return title;
	}
	@Parameter(required = true, description = "Title String of the plot", defaultValue = "Default Title")
	public void setTitle(String title) {
		this.title = title;
	}

	
}
