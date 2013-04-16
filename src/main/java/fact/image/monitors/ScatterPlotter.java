package fact.image.monitors;

import java.awt.BorderLayout;
import java.io.Serializable;

import javax.swing.JFrame;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.annotations.Parameter;
import stream.plotter.DataVisualizer;
import fact.Constants;
import fact.data.EventUtils;
import fact.image.OnlineStatistics;

/**
 * 
 * @author bruegge
 * 
 */
public class ScatterPlotter extends DataVisualizer {
	static Logger log = LoggerFactory.getLogger(ScatterPlotter.class);
	private ScatterPlotPanel scatterPlotter;
	JFrame frame;
	
	private String compValue = "";
	public String getCompValue() {
		return compValue;
	}
	public void setCompValue(String compValue) {
		this.compValue = compValue;
	}

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

	private boolean drawErrors = true;

	public boolean isDrawErrors() {
		return drawErrors;
	}

	@Parameter(required = true, description = "Flag to toggle drawing of Errorbars in plot.")
	public void setDrawErrors(boolean drawErrors) {
		this.drawErrors = drawErrors;
	}

	public ScatterPlotter() {
		width = 690;
		height = 460;
		// this.setHistory(1440);
		// prevDataItem = DataFactory.create();
	}

	OnlineStatistics onStat = null;
	private XYSeriesCollection dataset;
	private XYSeries[] sAr;

	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);
		scatterPlotter = new ScatterPlotPanel(compValue);
		frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(scatterPlotter, BorderLayout.CENTER);
		frame.setSize(width, height);
		frame.setVisible(true);
		dataset = new XYSeriesCollection();
		scatterPlotter.setDataset(dataset);
		sAr = new XYSeries[keys.length];
		for(int i = 0 ; i < keys.length; i++){
			sAr[i] = new XYSeries(keys[i]);
			dataset.addSeries(sAr[i]);
		}

	}

	@Override
	public Data processMatchingData(Data data) {

		double first = 0.0;
		if (data.containsKey(compValue)) {
				Serializable val = data.get(compValue);
				// in case the "key" describes a single value per event
				if (val.getClass().equals(float.class)
						|| val.getClass().equals(double.class)
						|| val.getClass().equals(int.class)
						|| val instanceof Number) {
		
					first = EventUtils.valueToDouble(val);
				} else if (val.getClass().isArray()) {
					log.info("This plotter cant handle arrays. Its just too much");
				}
		} else {
			log.info("The key " + compValue + " does not exist in the Event");
		}
		
		for (int i = 0; i < sAr.length; i++){
			XYSeries series = sAr[i];
			Serializable val = data.get(keys[i]);
			double y = 0;
			// in case the "key" describes a single value per event
			if(val == null){
				log.info(Constants.ERROR_WRONG_KEY + keys[i] + ",  " + this.getClass().getSimpleName() );
			} else if (val.getClass().equals(float.class)
					|| val.getClass().equals(double.class)
					|| val.getClass().equals(int.class)
					|| val instanceof Number)
			{
				y = EventUtils.valueToDouble(val);
			}
			series.add(first, y);
		}
		
		
//		series.add(rand.nextDouble(), rand.nextDouble());
//		scatterPlotter.setDataset(dataset);
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
}
