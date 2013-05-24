package fact.image.monitors;

import java.awt.BorderLayout;
import java.io.Serializable;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
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
import fact.data.EventUtils;
import fact.processors.CreateHistogram;

/**
 * 
 * @author bruegge
 * 
 */
public class HistogramPlotter extends DataVisualizer {
	static Logger log = LoggerFactory.getLogger(HistogramPlotter.class);
	JFrame frame;

	private boolean keepOpen = true;
	private String[] keys;
	private boolean drawErrors = true;
	private int numberOfBins = 10;
	private float maxBin= 500;
	private float minBin = 0;
//	private JFreeChart chart;
	float[] a = {0.2f,0.32f,0.323f};
	private IntervalXYDataset dataset;
	private XYPlot xyplot;
	private float interval;
	private CreateHistogram  cr = new CreateHistogram();

	public HistogramPlotter() {
		width = 690;
		height = 460;
	}


	@Override
	public void init(ProcessContext ctx) throws Exception {
		super.init(ctx);
		cr.setMaxBin(maxBin);
		cr.setNumberOfBins(numberOfBins);
		cr.setMinbin(minBin);
		cr.init(ctx);
		interval = (maxBin)/numberOfBins;
        final JFreeChart chart = ChartFactory.createXYBarChart(
                "Histogram",
                "X", 
                false,
                "#", 
                null,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
            );
        xyplot = chart.getXYPlot();
		final ChartPanel chartPanel = new ChartPanel(chart);
		frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(chartPanel, BorderLayout.CENTER);
		frame.setSize(width, height);
		frame.setVisible(true);
		

	}

	@Override
	public Data processMatchingData(Data data) {
		
		for (String key : getKeys()) {
			if (data.containsKey(key)) {
				Serializable val = data.get(key);
				// in case the "key" describes a single value per event
				if(val.getClass().isArray()){
					Class<?> comp = val.getClass().getComponentType();
					if (comp.equals(float.class)
							|| comp.equals(double.class)) 
					{
						a = EventUtils.toFloatArray(val);
						int[] bins = cr.processSeries(a);
						fillDataSet(bins);

					}
					else if(comp.equals(int.class)){
						a = EventUtils.toFloatArray(val);
						int[] bins = cr.processSeries(a);
						fillDataSet(bins);
					}
				}
				
			} else {
				log.info("The key " + key + " does not exist in the Event");
			}
	}
		return data;
	}

	private void fillDataSet(int[] bins) {
		XYSeries series = new XYSeries("");
        for (int i = 0; i < bins.length; ++i) {
            if (bins[i] > 0){
                    series.add(i * interval, bins[i]);
            }
        }		
        dataset = new XYSeriesCollection(series) {
            private static final long serialVersionUID = 1L;

            @Override
            public double getStartXValue(int series, int item) {
                    return interval * item;
            }

            @Override
            public double getEndXValue(int series, int item) {
                    return interval * (item + 1) - 1;
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
	

	public String[] getKeys() {
		return keys;
	}

	@Parameter(required = false, description = "The attributes/features to be plotted (non-numerical features will be ignored)")
	public void setKeys(String[] keys) {
		this.keys = keys;
	}
	

	public boolean isDrawErrors() {
		return drawErrors;
	}

	@Parameter(required = true, description = "Flag to toggle drawing of Errorbars in plot.")
	public void setDrawErrors(boolean drawErrors) {
		this.drawErrors = drawErrors;
	}


	public float getMinBin() {
		return minBin;
	}


	public void setMinBin(float minBin) {
		this.minBin = minBin;
	}


	public float getMaxBin() {
		return maxBin;
	}


	public void setMaxBin(float maxBin) {
		this.maxBin = maxBin;
	}


	public int getNumberOfBins() {
		return numberOfBins;
	}


	public void setNumberOfBins(int numberOfBins) {
		this.numberOfBins = numberOfBins;
	}
}
