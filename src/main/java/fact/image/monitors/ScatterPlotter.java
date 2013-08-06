package fact.image.monitors;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.annotations.Parameter;
import stream.plotter.DataVisualizer;
import fact.data.EventUtils;

/**
 * 
 * @author bruegge
 * 
 */
public class ScatterPlotter extends DataVisualizer {
	static Logger log = LoggerFactory.getLogger(ScatterPlotter.class);
	//	JFrame frame;

	private XYSeriesCollection dataset;
	private XYSeries series;

	private String xValue = "";
	private String yValue = "";

	private boolean keepOpen = true;

	private int i;
	private double x;

	private double y;

	public ScatterPlotter() {
		dataset = new XYSeriesCollection();
		series = new XYSeries("data", false);
//		series.add(2, 2); //Point 4
		dataset.addSeries(series);
		showGraph();
	}

	private void showGraph() {
		final JFreeChart chart = ChartFactory.createScatterPlot(
				"Title",                  // chart title
				"X",                      // x axis label
				"Y",                      // y axis label
				dataset,                  // data
				PlotOrientation.VERTICAL,
				true,                     // include legend
				true,                     // tooltips
				false                     // urls
				);
		XYPlot plot = (XYPlot) chart.getPlot();
		DemoRenderer renderer = new DemoRenderer();
//		renderer.setSeriesLinesVisible(0, true);
		plot.setRenderer(renderer);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(640, 480));
		final ApplicationFrame frame = new ApplicationFrame("Title");
		frame.setContentPane(chartPanel);
		frame.pack();
		frame.setVisible(true);
	}


	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);
	}

	@Override
	public Data processMatchingData(Data data) {
		if (data.containsKey(xValue) && data.containsKey(yValue)) {
			x = (Double) data.get(xValue);
			y = (Double) data.get(yValue);
		} else {
			log.info("The key " + xValue +  "  or " + yValue + " does not exist in the Event");
		}
//		System.out.println(x);
//		System.out.println(y);
		series.add(x,y);
		//		
		//		series.add(2, 3);
		//		series.add(2, 4);
		//		series.add(2, 5);
		//		series.add(2, 6);
		//		series.add(2, 7);

		//		dataset.addSeries(series);
		return data;
	}

	@Override
	public void finish() throws Exception {
		if (!keepOpen) {
			log.debug("Closing plot frame");
			//			frame.setVisible(false);
			//			frame.dispose();
			//			frame = null;
		} else {
			log.debug("Keeping plot frame visible...");
		}
	}

	class DemoRenderer extends XYShapeRenderer {
		private static final long serialVersionUID = 4804521867675934134L;

		@Override
		public java.awt.Shape getSeriesShape(int series){
			return new Rectangle2D.Double(-2, -2, 4, 4);
			
		}
	}

	public boolean isKeepOpen() {
		return keepOpen;
	}

	@Parameter(required = true, description = "Flag indicates wther the window stays open after the process has finished", defaultValue = "true")
	public void setKeepOpen(boolean keepOpen) {
		this.keepOpen = keepOpen;
	}

	public String getxValue() {
		return xValue;
	}
	public void setxValue(String xValue) {
		this.xValue = xValue;
	}
	
	public String getyValue() {
		return yValue;
	}
	public void setyValue(String yValue) {
		this.yValue = yValue;
	}
}
