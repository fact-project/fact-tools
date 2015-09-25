package fact.plotter;

import fact.container.Histogram1D;
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

import javax.swing.*;
import java.awt.*;

/**
 * Created by jebuss on 24.09.15.
 */
public class PlotPixelHistogram1D extends DataVisualizer {
    static Logger log = LoggerFactory.getLogger(HistogramPlotter.class);
    JFrame frame;

    @Parameter(required = true)
    private String key;

    @Parameter(required = false)
    private boolean logAxis = false;

    @Parameter(required = false)
    private String title = "Histogram";

    @Parameter(required = false)
    private String color = "#666699";

    @Parameter(required = false)
    private String xTitle = null;

    @Parameter(required = false)
    private String yTitle = null;

    @Parameter(required = false)
    private int chid = 0;

    private boolean keepOpen = true;

    private JFreeChart chart;
    private XYSeries dataSeries;

    private IntervalXYDataset dataset;



    public PlotPixelHistogram1D() {
        width = 690;
        height = 460;
    }

    @Override
    public void init(ProcessContext ctx) throws Exception {
        super.init(ctx);

        if (xTitle == null){ xTitle = key; }
        if (yTitle == null){ yTitle = "#"; }

        dataSeries  = new XYSeries(key);
        dataset     = new XYSeriesCollection(dataSeries);
        chart       = createChart();

    }

    @Override
    public Data processMatchingData(Data data) {
//		Utils.isKeyValid( data, key, Double.class);
        Histogram1D[] histograms;

        if(data.containsKey(key)){
            histograms = (Histogram1D[]) data.get(key);
        } else {
            throw new RuntimeException("Key not found in event. "  + key  );
        }

        dataSeries.clear();

        Histogram1D hist = histograms[chid];

        double[][] XYArray = hist.toArray();

        for (int i = 0; i < XYArray[0].length; i++){
            if(Double.isNaN(XYArray[0][i]) || Double.isNaN(XYArray[1][i]) ){
                log.warn("This doesnt handle NaNs very well.");
            }
            try{
                dataSeries.add(XYArray[0][i], XYArray[1][i]);
            } catch(RuntimeException e ) {
                log.warn("cannot add value to series");
            }
        }

        try {
            dataset = new XYSeriesCollection(dataSeries);
            chart.setTitle(title + " " + key + "    " + hist.getnEvents() + " events");
        } catch( RuntimeException e ) {
            log.warn("cannot plot series");
        }

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

    private JFreeChart createChart(){
        chart = ChartFactory.createXYBarChart(
                this.title,
                this.xTitle,
                false,
                this.yTitle,
                this.dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        chart.setBackgroundPaint(new Color(230,230,230));
        XYPlot xyplot = (XYPlot)chart.getPlot();

        if(logAxis)
            xyplot.setRangeAxis(new LogarithmicAxis(this.yTitle));

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

        return chart;
    }


    public boolean isKeepOpen() {
        return keepOpen;
    }

    @Parameter(required = false, description = "Flag indicates whether the window stays open after the process has finished", defaultValue = "true")
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
    @Parameter(required = false, description = "Flag to indicate wether the y-Axis should be in logarithmic units")
    public void setLogAxis(boolean logAxis) {
        this.logAxis = logAxis;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setxTitle(String xTitle) {
        this.xTitle = xTitle;
    }

    public void setyTitle(String yTitle) {
        this.yTitle = yTitle;
    }

    public void setChid(int chid) {
        this.chid = chid;
    }
}
