package fact.image.monitors;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
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

	private float max= 10;
	private float min = 0;
	private boolean logAxis = false;

	private IntervalXYDataset dataset;
	private XYPlot xyplot;
	private float binSize;
	private String title;
	private String color = "#666699";

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
		chart.setTitle(title);
		final XYBarRenderer r = (XYBarRenderer) xyplot.getRenderer();
		r.setDrawBarOutline(false);
		r.setShadowVisible(false);
//		r.setDefaultShadowsVisible(false);
		r.setMargin(0.05);
		r.setBarPainter(new StandardXYBarPainter());
		try{
			Color c = Color.decode(color);
			r.setSeriesPaint(0, c);
		} catch(NumberFormatException e){
			log.warn("Could not parse the color string. has to look like: #f0f0f0");
		}
		
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
				binSize = max/(hist.length);
				fillDataSet(hist);
				xyplot.getDomainAxis().setRange(min - binSize, max + binSize);
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
				return binSize * (item + 1) ;
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


	public float getMin() {
		return min;
	}
	public void setMin(float minBin) {
		this.min = minBin;
	}


	public float getMax() {
		return max;
	}
	public void setMax(float maxBin) {
		this.max = maxBin;
	}

	public boolean isLogAxis() {
		return logAxis;
	}
	public void setLogAxis(boolean logAxis) {
		this.logAxis = logAxis;
	}


	public String getTitle() {
		return title;
	}
	@Parameter(required = false, description = "The title string of the window")
	public void setTitle(String title) {
		this.title = title;
	}


	public String getColor() {
		return color;
	}
	@Parameter(required = false, description = "The color of the bars to be drawn #f4f4f4")
	public void setColor(String color) {
		this.color = color;
	}

}
