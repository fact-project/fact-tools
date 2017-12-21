package fact.plotter;

import fact.Utils;
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

import javax.swing.*;
import java.awt.*;

/**
 * This plotter class needs only two parameters. The binWidth and the key to the data.
 *
 * @author Kai Bruegge &lt;kai.bruegge@tu-dortmund.de&gt;
 */
public class HistogramPlotter extends DataVisualizer {
    static Logger log = LoggerFactory.getLogger(HistogramPlotter.class);
    JFrame frame;

    @Parameter(required = true, description = "The attributes/features to be plotted (non-numerical features will be ignored)")
    private String key;

    @Parameter(required = false, description = "Flag indicates whether the window stays open after the process has finished", defaultValue = "true")
    private boolean keepOpen = true;

    @Parameter(required = false, description = "Flag to indicate wether the y-Axis should be in logarithmic units")
    private boolean logAxis = false;

    @Parameter(required = false, description = "The title string of the window")
    private String title = "Histogram";

    @Parameter(required = false, description = "The color of the bars to be drawn #f4f4f4")
    private String color = "#666699";

    private double binWidth = 0.5;
    private SimpleHistogramDataset dataset;
    private JFreeChart chart;
    private long counter = 0;

    public HistogramPlotter() {
        width = 690;
        height = 460;
    }


    @Override
    public void init(ProcessContext ctx) throws Exception {
        super.init(ctx);

        dataset = new SimpleHistogramDataset(key);

        chart = ChartFactory.createHistogram(
                title,
                key,
                "#",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        chart.setBackgroundPaint(new Color(230, 230, 230));
        XYPlot xyplot = (XYPlot) chart.getPlot();
        if (logAxis)
            xyplot.setRangeAxis(new LogarithmicAxis("#"));

        chart.setTitle(title);
        xyplot.setForegroundAlpha(0.7F);
        xyplot.setBackgroundPaint(Color.WHITE);
        xyplot.setDomainGridlinePaint(new Color(150, 150, 150));
        xyplot.setRangeGridlinePaint(new Color(150, 150, 150));
        XYBarRenderer xybarrenderer = (XYBarRenderer) xyplot.getRenderer();
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
//		Utils.isKeyValid( data, key, Double.class);
        double v = 0;
        if (data.containsKey(key)) {
            v = Utils.valueToDouble(data.get(key));
        } else {
            throw new RuntimeException("Key not found in event. " + key);
        }
        if (Double.isNaN(v)) {
            log.warn("This doesnt handle NaNs very well.");
        }
        try {
            dataset.addObservation(v);
            chart.setTitle(title + " " + key + "    " + counter++ + " entries");
        } catch (RuntimeException e) {
            //log.debug("RuntimeException while trying to add observation. Probably a missing bin for the value. Trying to create a new bin");
            SimpleHistogramBin bin = new SimpleHistogramBin(Math.floor(v / binWidth) * binWidth, Math.floor(v / binWidth) * binWidth + binWidth - 1e-17, true, false);
            //The 1e-17 in the upper limit avoids rounding errors which lead to overlapping bins and therefore missing histogram entries
            try {
                dataset.addBin(bin);
                dataset.addObservation(v);
                chart.setTitle("Histogram " + key + "    " + counter++ + " entries");
            } catch (Exception ee) {
                log.warn("Overlapping bin");
            }
        }
        //else if ( Utils.isKeyValid(data, key, Double.class) ) {
        //dataset.addObservation(((Double) data.get(key)).floatValue());
        //}

        //dataset.addObservations(Utils.toDoubleArray(data.get(key)));
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



}
