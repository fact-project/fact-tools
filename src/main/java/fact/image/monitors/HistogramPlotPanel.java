package fact.image.monitors;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.category.StatisticalBarRenderer;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.plotter.PlotPanel;

public class HistogramPlotPanel extends JPanel {
	private static final long serialVersionUID = -135743189148617433L;
	static Logger log = LoggerFactory.getLogger(PlotPanel.class);
//	private boolean showErrorBars = true;
	private CategoryPlot plot;
	
		

	private DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
	public DefaultStatisticalCategoryDataset getDataset() {
		return dataset;
	}
	public void setDataset(DefaultStatisticalCategoryDataset dataset) {
		this.dataset = dataset;
		//update chart here
		plot.setDataset(dataset);
//		CustomStatisticalBarRender render = (CustomStatisticalBarRender) plot.getRenderer();
//		for(int i = 0 ; i < plot.getCategories().size(); i++) {
//			int rgb = Color.HSBtoRGB(((float)i)/plot.getCategories().size(), 0.7f, 0.95f);
//			render.setSeriesPaint(i, new Color(rgb));
//		}
	}
	
	public HistogramPlotPanel(boolean drawError){
		JFreeChart freeChart = ChartFactory.createBarChart("Test", "selected Keys", "Mean ", dataset, PlotOrientation.VERTICAL, false, false,false);
		plot = freeChart.getCategoryPlot();

		if(drawError)
		{
			StatisticalBarRenderer render = new CustomStatisticalBarRender();
			plot.setRenderer(render);
		} 
		else
		{
			BarRenderer render = new CustomBarRender();
			render.setBarPainter(new StandardBarPainter());
//			render.setDrawBarOutline(false);
			render.setShadowVisible(false);
//			render.setBaseFillPaint(Color.red);
//			render.setGradientPaintTransformer(null);
			plot.setRenderer(render);
		}
		this.setLayout(new BorderLayout());
		final ChartPanel p = new ChartPanel(freeChart);
		add(p,BorderLayout.CENTER);
	}
	
	
	

}
