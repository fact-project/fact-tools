package fact.image.monitors;

import java.awt.geom.Rectangle2D;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

public class ScatterPlotPanel {
    private XYSeriesCollection dataset;
	private XYSeries data;

    public ScatterPlotPanel () {
        dataset = new XYSeriesCollection();
        data = new XYSeries("data");
        data.add(2, 2); //Point 4
        dataset.addSeries(data);
        showGraph();
    }

    private void showGraph() {
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        final ApplicationFrame frame = new ApplicationFrame("Title");
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }

    private JFreeChart createChart(final XYDataset dataset) {
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
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        plot.setRenderer(renderer);
        return chart;
    }

	public XYSeries getData() {
		return data;
	}

	public void setData(XYSeries data) {
		this.data = data;
	}

}

class DemoRenderer extends XYShapeRenderer {
	private static final long serialVersionUID = 4804521867675934134L;

	@Override
	public java.awt.Shape getSeriesShape(int series){
		return new Rectangle2D.Double(-2, -2, 4, 4);
	}
}
