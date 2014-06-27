package fact.plotter;

import fact.Utils;
import fact.image.OnlineStatistics;
import fact.plotter.ui.BarPlotPanel;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.annotations.Parameter;
import stream.plotter.DataVisualizer;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

/**
 * This class can plot a bar graph with errorBars by calculating the mean and
 * standarddeviation for a each key and event. If one of the keys refer to an
 * array. The same calculation will be done for every item in the array.
 * 
 * @author bruegge
 * 
 */
public class BarPlotter extends DataVisualizer {
	static Logger log = LoggerFactory.getLogger(BarPlotter.class);
	private BarPlotPanel histPanel;
	JFrame frame;

	private boolean keepOpen = true;
	private boolean drawErrors = true;
	private String[] keys;
	private String title = "Default Title";
	
	
	

	public BarPlotter() {
		width = 690;
		height = 460;
		// this.setHistory(1440);
		// prevDataItem = DataFactory.create();
	}

	OnlineStatistics onStat = null;

	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);
		onStat = new OnlineStatistics();
		histPanel = new BarPlotPanel(drawErrors, title);
		frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(histPanel, BorderLayout.CENTER);
		frame.setSize(width, height);
		frame.setVisible(true);
//		frame.setTitle(title);
		if(keys==null){
			log.error("The keys paramter was null. Did you set it in the .xml file?");
		}
	}

	@Override
	public Data processMatchingData(Data data) {
		DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
		
		for (String key : getKeys()) {

			if (data.containsKey(key)) {
				Serializable val = data.get(key);
				// in case the "key" describes a single value per event
				if (val.getClass().equals(float.class)
						|| val.getClass().equals(double.class)
						|| val.getClass().equals(int.class)
						|| val instanceof Number) {
					double[] a = { Utils.valueToDouble(val) };
					onStat.updateValues(key, a);
					dataset.add(onStat.getValueMap().get(key + "_avg")[0],
							onStat.getValueMap().get(key + "_stdErr")[0], "",
							key);
				} else if (val.getClass().isArray()) {
					Class<?> clazz = val.getClass().getComponentType();
					if (clazz.equals(float.class) || clazz.equals(double.class)
							|| clazz.equals(int.class)) {
						double[] valArray = Utils.toDoubleArray(val);
						onStat.updateValues(key, valArray);
						for (int i = 0; i < onStat.getValueMap().get(
								key + "_avg").length; i++) {
							dataset.add(
									onStat.getValueMap().get(key + "_avg")[i],
									onStat.getValueMap().get(key + "_stdErr")[i],
									key + i, key);
						}
					}
				}
			} else {
				log.error("The key " + key + " does not exist in the Event");
				throw new RuntimeException("Key not found in event. "  + key);
			}
		}
		histPanel.setDataset(dataset);
		histPanel.getPreferredSize();
		return data;
	}

	@Override
	public void finish() throws Exception {
		if (!keepOpen) {
			log.debug("Closing plot frame");
			frame.setVisible(false);
			frame.dispose();
			frame = null;
		} else {
			log.debug("Keeping plot frame visible...");
		}
	}
	
	
	public boolean isKeepOpen() {
		return keepOpen;
	}

	@Parameter(required = true, description = "Flag indicates wther the window stays open after the process has finished", defaultValue = "true")
	public void setKeepOpen(boolean keepOpen) {
		this.keepOpen = keepOpen;
	}


	public String[] getKeys() {
		return keys;
	}

	@Parameter(required = false, description = "The attributes/features to be plotted (non-numerical features will be ignored)")
	public void setKeys(String[] keys) {
		this.keys = keys;
	}


	public boolean isDrawErrors() {
		return drawErrors;
	}

	@Parameter(required = true, description = "Flag to toggle drawing of Errorbars in plot.")
	public void setDrawErrors(boolean drawErrors) {
		this.drawErrors = drawErrors;
	}

	public String getTitle() {
		return title;
	}
	@Parameter(required = true, description = "Title String of the plot", defaultValue = "Default Title")
	public void setTitle(String title) {
		this.title = title;
	}
}
