package fact.image.monitors;

import java.awt.BorderLayout;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.plotter.PlotPanel;

public class ScatterPlotPanel extends JPanel {
	private static final long serialVersionUID = -135743189148617433L;
	static Logger log = LoggerFactory.getLogger(PlotPanel.class);
//	private boolean showErrorBars = true;
	private XYPlot plot;
	
		

	private XYDataset dataset = new DefaultXYDataset();
	public XYDataset getDataset() {
		return dataset;
	}
	public void setDataset(XYDataset dataset) {
		this.dataset = dataset;
		//update chart here
		plot.setDataset(dataset);
		
	}
	
	public ScatterPlotPanel(String key){
		JFreeChart freeChart = ChartFactory.createScatterPlot("TestScatterPlot", "key", " ", dataset, PlotOrientation.HORIZONTAL, true, true, false);
		plot = (XYPlot) freeChart.getPlot();
		XYItemRenderer render = new  DemoRenderer();
//		render.setShape(new Rectangle2D.Double(-2, -2, 4, 4));
		plot.setRenderer(render);
		this.setLayout(new BorderLayout());
		final ChartPanel p = new ChartPanel(freeChart);
		add(p,BorderLayout.CENTER);
	}
	
	
	

}

class DemoRenderer extends XYShapeRenderer {
	private static final long serialVersionUID = 4804521867675934134L;

	@Override
	public java.awt.Shape getSeriesShape(int series){
		return new Rectangle2D.Double(-2, -2, 4, 4);
		
	}
}
