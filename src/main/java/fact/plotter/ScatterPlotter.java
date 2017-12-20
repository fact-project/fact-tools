package fact.plotter;

import fact.Utils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
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

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author bruegge
 */
public class ScatterPlotter extends DataVisualizer {
    static Logger log = LoggerFactory.getLogger(ScatterPlotter.class);
    //	JFrame frame;

    private XYSeriesCollection dataset;
    private XYSeries series;

    private String xValue = "";
    private String yValue = "";

    @Parameter(required = true, description = "Title String of the plot", defaultValue = "Default Title")
    public String title = "Default Title";

    @Parameter(required = true, description = "Flag indicates wther the window stays open after the process has finished", defaultValue = "true")
    public boolean keepOpen = true;

    private String color = "#2A2EE0";
    //	private int i;
    private double x;

    private double y;

    private void showGraph() {
        final JFreeChart chart = ChartFactory.createScatterPlot(
                title,                  // chart title
                xValue,                      // x axis label
                yValue,                      // y axis label
                dataset,                  // data
                PlotOrientation.VERTICAL,
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        DemoRenderer renderer = new DemoRenderer();
//		renderer.setBasePaint(Color.blue);
//		renderer.setSeriesLinesVisible(0, true);
        plot.setRenderer(renderer);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(640, 480));
        final ApplicationFrame frame = new ApplicationFrame(title);
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }


    @Override
    public void init(ProcessContext ctx) throws Exception {
        dataset = new XYSeriesCollection();
        series = new XYSeries("data", false);
//		series.add(2, 2); //Point 4
        dataset.addSeries(series);
        showGraph();
        super.init(ctx);
    }

    @Override
    public Data processMatchingData(Data data) {
        if (data.containsKey(xValue) && data.containsKey(yValue)) {
            if (data.containsKey(xValue)) {
                x = Utils.valueToDouble(data.get(xValue));
            } else {
                throw new RuntimeException("Key for xValue not found in event. " + xValue);
            }
            if (Double.isNaN(x)) {
                log.warn("This doesnt handle NaNs very well.");
            }

            if (data.containsKey(yValue)) {
                y = Utils.valueToDouble(data.get(yValue));
            } else {
                throw new RuntimeException("Key for yValue not found in event. " + yValue);
            }
            if (Double.isNaN(y)) {
                log.warn("This doesnt handle NaNs very well.");
            }

        } else {
            log.info("The key " + xValue + "  or " + yValue + " does not exist in the Event");
        }

        series.add(x, y);
        //

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
        public java.awt.Shape getSeriesShape(int series) {
            return new Rectangle2D.Double(-1, -1, 2, 2);

        }

        @Override
        public java.awt.Paint getSeriesPaint(int series) {
            try {
                Color c = Color.decode(color);
                return c;
            } catch (NumberFormatException e) {
                log.warn("Could not decode Colorstring. String should look like this: #FAFAFA");
                return Color.blue;
            }
        }
    }
}
