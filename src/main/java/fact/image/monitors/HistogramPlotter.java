package fact.image.monitors;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.annotations.Parameter;
import stream.plotter.DataVisualizer;

/**
 * 
 * @author bruegge
 * 
 */
public class HistogramPlotter extends DataVisualizer {
	static Logger log = LoggerFactory.getLogger(HistogramPlotter.class);
	JFrame frame;

	private boolean keepOpen = true;
	private String key;
	private boolean drawErrors = true;
	private float max= 500;
	private float min = 0;
	private boolean logAxis = false;
	//	private JFreeChart chart;
	//	float[] a = {0.2f,0.32f,0.323f};
	private IntervalXYDataset dataset;
	private XYPlot xyplot;
	private float binSize;

	public HistogramPlotter() {
		width = 690;
		height = 460;
	}


	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);
//		binSize = (max)/numberOfBins;
		final JFreeChart chart = ChartFactory.createXYBarChart(
				"Histogram",
				key, 
				false,
				"#", 
				null,
				PlotOrientation.VERTICAL,
				true,
				true,
				false
				);
		xyplot = chart.getXYPlot();
		if(logAxis)
			xyplot.setRangeAxis(new LogarithmicAxis("#"));
		
		final ChartPanel chartPanel = new ChartPanel(chart);
		frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(chartPanel, BorderLayout.CENTER);
		frame.setSize(width, height);
		frame.setVisible(true);


	}

	@Override
	public Data processMatchingData(Data data) {
		if(getKey()==null){
			log.warn("No keys specified for HistogramPLotter");
			return null;
		}
		try{
			if (data.containsKey(key)) {
				int[] hist = (int[]) data.get(key);
				binSize = max/hist.length;
				fillDataSet(hist);
			}
		} catch (ClassCastException e){
			log.error("Key did not refer to an int array");
			return null;
		}
		return data;
	}

	private void fillDataSet(int[] bins) {
		XYSeries series = new XYSeries("");
		for (int i = 0; i < bins.length; ++i) {
			if (bins[i] > 0){
				series.add(i * binSize, bins[i]);
			}
		}		
		dataset = new XYSeriesCollection(series) {
			private static final long serialVersionUID = 1L;

			@Override
			public double getStartXValue(int series, int item) {
				return binSize * item;
			}

			@Override
			public double getEndXValue(int series, int item) {
				return binSize * (item + 1) - 1;
			}

		};
		xyplot.setDataset(dataset);

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


	public String getKey() {
		return key;
	}

	@Parameter(required = true, description = "The attributes/features to be plotted (non-numerical features will be ignored)")
	public void setKey(String key) {
		this.key = key;
	}


	public boolean isDrawErrors() {
		return drawErrors;
	}

	@Parameter(required = true, description = "Flag to toggle drawing of Errorbars in plot.")
	public void setDrawErrors(boolean drawErrors) {
		this.drawErrors = drawErrors;
	}


	public float getMinBin() {
		return min;
	}


	public void setMinBin(float minBin) {
		this.min = minBin;
	}


	public float getMaxBin() {
		return max;
	}


	public void setMaxBin(float maxBin) {
		this.max = maxBin;
	}

	public boolean isLogAxis() {
		return logAxis;
	}


	public void setLogAxis(boolean logAxis) {
		this.logAxis = logAxis;
	}

}
