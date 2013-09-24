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
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.annotations.Parameter;
import stream.plotter.DataVisualizer;
import fact.data.EventUtils;

/**
 * 
 * This plotter takes an int[] and interprets its values as a histogram. Meaning every entry in the array is a dimensionless counter for something.
 * 
 * 
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 * 
 */
public class HistogramTest extends DataVisualizer {
	static Logger log = LoggerFactory.getLogger(HistogramTest.class);
	JFrame frame;

	private boolean keepOpen = true;
	private String key;

	private double binWidth = 0.5f;
	
	private boolean logAxis = false;

	private SimpleHistogramDataset dataset;
	private String title;
	private String color = "#666699";
	private JFreeChart chart;
	private int counter = 0;

	public HistogramTest() {
		width = 690;
		height = 460;
	}



	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);
		
	    dataset = new SimpleHistogramDataset(key);
	   
	    chart = ChartFactory.createHistogram(
	              "Histogram",
	              key,
	              "#",
	              dataset,
	              PlotOrientation.VERTICAL,
	              true,
	              true,
	              false
	          );

	    chart.setBackgroundPaint(new Color(230,230,230));
	    XYPlot xyplot = (XYPlot)chart.getPlot();
		if(logAxis)
			xyplot.setRangeAxis(new LogarithmicAxis("#"));

		chart.setTitle(title);
	    xyplot.setForegroundAlpha(0.7F);
	    xyplot.setBackgroundPaint(Color.WHITE);
	    xyplot.setDomainGridlinePaint(new Color(150,150,150));
	    xyplot.setRangeGridlinePaint(new Color(150,150,150));
	    XYBarRenderer xybarrenderer = (XYBarRenderer)xyplot.getRenderer();
	    xybarrenderer.setShadowVisible(false);
	    xybarrenderer.setBarPainter(new StandardXYBarPainter());
//	    xybarrenderer.setDrawBarOutline(false);
	    
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

		if(  EventUtils.isKeyValid(data, key, Double.class)){
			double v = (Double) data.get(key);
			try{
				dataset.addObservation(v);
				chart.setTitle("Histogram " + key + "    " + counter++ + " entries");
			} catch(RuntimeException e ) {
				
				SimpleHistogramBin bin = new SimpleHistogramBin(Math.floor(v/binWidth)*binWidth, Math.floor(v/binWidth)*binWidth + binWidth, false, false);
				dataset.addBin(bin);
				dataset.addObservation(v);
				chart.setTitle("Histogram " + key + "    " + counter++ + " entries");
			}
		} 
		//else if ( EventUtils.isKeyValid(data, key, Double.class) ) {
			//dataset.addObservation(((Double) data.get(key)).floatValue());
		//}
		
		//dataset.addObservations(EventUtils.toDoubleArray(data.get(key)));
//		try{
//			if (data.containsKey(key)) {
//				int[] hist = (int[]) data.get(key);
//				binSize = max/(hist.length);
//				fillDataSet(hist);
//				xyplot.getDomainAxis().setRange(min - binSize, max + binSize);
//			}
//		} catch (ClassCastException e){
//			log.error("Key did not refer to an int array");
//			return null;
//		}
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


	public String getKey() {
		return key;
	}
	@Parameter(required = true, description = "The attributes/features to be plotted (non-numerical features will be ignored)")
	public void setKey(String key) {
		this.key = key;
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



	public double getBinWidth() {
		return binWidth;
	}



	public void setBinWidth(double binWidth) {
		this.binWidth = binWidth;
	}

}
