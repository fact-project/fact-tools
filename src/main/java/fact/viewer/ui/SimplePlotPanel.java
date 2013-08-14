/**
 * 
 */
package fact.viewer.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris
 * 
 */
public class SimplePlotPanel extends JPanel {

	/** The unique class ID */
	private static final long serialVersionUID = -4365922853856318209L;

	protected float redCount = 0.0f;
	static Logger log = LoggerFactory.getLogger(SimplePlotPanel.class);
	final JLabel valueLabel = new JLabel("Value: ");
	final XYPlot plot;
	private ValueMarker marker;

	public XYPlot getPlot() {
		return plot;
	}

	private XYSeriesCollection series = new XYSeriesCollection();

	public XYSeriesCollection getSeries() {
		return series;
	}

	public void setSeries(XYSeriesCollection series) {
		this.series = series;
	}

	private XYItemRenderer render;
	List<ValueListener> listener = new ArrayList<ValueListener>();

	public SimplePlotPanel() {
		setLayout(new BorderLayout());

		JPanel fp = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fp.add(valueLabel);
		add(fp, BorderLayout.NORTH);

		setBorder(BorderFactory.createEtchedBorder()); // .setBorderColor( null
														// );
		/**
		 * draw a shape at each point
		 */
		render = new XYLineAndShapeRenderer(true, true);

		plot = new XYPlot(series, new NumberAxis("Slice"), new NumberAxis(),
				render); // new StandardXYItemRenderer()
							// );
							// //PlotOrientation.HORIZONTAL,
							// false, false, false );

		/**
		 * enable panning
		 */
		plot.setDomainPannable(true);
		plot.setRangePannable(true);
		
		marker = new ValueMarker(1.0);
		marker.setPaint(Color.gray);
		marker.setStroke(new BasicStroke(1.0f, // Width
				BasicStroke.CAP_SQUARE, // End cap
				BasicStroke.JOIN_MITER, // Join style
				10.0f, // Miter limit
				new float[] { 10.0f, 10.0f }, // Dash pattern
				0.0f)); // Dash phase
		marker.setLabel("Slice");
		plot.addDomainMarker(marker);
		// Add some sort of interval rendering here-. 

		
		final JFreeChart chart = new JFreeChart(plot);
		final ChartPanel p = new ChartPanel(chart);
		p.addChartMouseListener(new ChartMouseListener() {

			@Override
			public void chartMouseClicked(ChartMouseEvent arg0) {
			}

			@Override
			public void chartMouseMoved(ChartMouseEvent arg0) {
				// log.info( "chartMouseMoved: {}", arg0 );
				MouseEvent me = arg0.getTrigger();
				/*
				 * log.info( "offsets: {}", offsets ); log.info(
				 * "domainAxisEdge: {}", plot.getDomainAxisEdge() ); log.info(
				 * "rangeAxisLocation: {}", plot.getRangeAxisLocation() );
				 * log.info( "domainAxisLocation: {}",
				 * plot.getDomainAxisLocation() ); log.info( "p.insets: {}",
				 * p.getInsets() ); log.info( "axis.offset.left: {}",
				 * plot.getAxisOffset().getLeft() );
				 */
				NumberAxis domain = (NumberAxis) plot.getDomainAxis();
				Rectangle2D chartArea = p.getChartRenderingInfo().getPlotInfo()
						.getDataArea();
				Double xval = domain.java2DToValue((double) me.getPoint().x,
						chartArea, plot.getDomainAxisEdge());
				Double yval = plot.getRangeAxis().java2DToValue(
						(double) me.getPoint().y, chartArea,
						plot.getRangeAxisEdge());

				valueLabel.setText("Value: " + yval);

				if (me.isShiftDown()) {
					for (ValueListener v : listener) {
						v.selectedValue(xval, yval);
					}
				}
			}

		});

		p.setBackground(Color.white);
		setBackground(Color.LIGHT_GRAY);
		((XYLineAndShapeRenderer) (getRender())).setBaseShapesVisible(false);
		/**
		 * add to context menu
		 */
		JCheckBoxMenuItem mI = new JCheckBoxMenuItem("Show Ticks", false);
		mI.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// (XYLineAndShapeRenderer) plot.getRenderer();
				// render.setBaseShapesVisible(
				// !((XYLineAndShapeRenderer)(plot.getRenderer()).getBaseShapesVisible())
				// );

				((XYLineAndShapeRenderer) (getRender()))
						.setBaseShapesVisible(!((XYLineAndShapeRenderer) (getRender()))
								.getBaseShapesVisible());

			}
		});
		p.getPopupMenu().add(mI);
		add(p, BorderLayout.CENTER);
	}

	public ValueMarker getMarker() {
		return marker;
	}

	public void setMarker(ValueMarker marker) {
		this.marker = marker;
	}

	public void addValueListener(ValueListener v) {
		listener.add(v);
	}

	public void clearSeries() {

		redCount = .0f;
		series.removeAllSeries();
	}

	public void plot(String name, double[] data) {
		series.removeAllSeries();
		series.addSeries(createSeries(name, data));
		plot.datasetChanged(null);
	}

	public void addSeries(String name, double[] data) {
		series.addSeries(createSeries(name, data));
	}

	public void addSeries(String name, double[] data, int start, int end) {
		series.addSeries(createSeries(name, data, start, end));
	}

	public void addSeries(String name, Color c, float[] data) {
		series.addSeries(createSeries(name, data));
		plot.getRenderer().setSeriesPaint(series.getSeriesIndex(name),
				c);
		// plot.getRenderer().setSeriesPaint(series.getSeriesIndex(name),
		// Color.green);
	}

	public void addSeries(String name, Color c, float[] data, int start, int end) {

		series.addSeries(createSeries(name, data, start, end));
		plot.getRenderer().setSeriesPaint(series.getSeriesIndex(name),
				c);
		// plot.getRenderer().setSeriesPaint(series.getSeriesIndex(name),
		// Color.green);
	}



	public XYDataset createDataset(String name, double[] data) {
		return new XYSeriesCollection(createSeries(name, data));
	}

	public XYSeries createSeries(String name, float[] data) {
		XYSeries series = new XYSeries(name);
		for (int i = 0; i < data.length; i++) {
			series.add((double) i, data[i]);
		}
		// plot.getRenderer().getser
		return series;
	}

	public XYSeries createSeries(String name, float[] data, int start, int end) {
		XYSeries series = new XYSeries(name);
		// reclac i to fit domain from 0 to roi;
		for (int i = start; i < end; i++) {
			series.add((double) (i - start), data[i]);
		}
		return series;
	}

	public XYSeries createSeries(String name, double[] data) {
		XYSeries series = new XYSeries(name);
		for (int i = 0; i < data.length; i++) {
			series.add((double) i, data[i]);
		}
		return series;
	}

	public XYSeries createSeries(String name, double[] data, int start, int end) {
		XYSeries series = new XYSeries(name);
		// reclac i to fit domain from 0 to roi;
		for (int i = start; i < end; i++) {
			series.add((double) (i - start), data[i]);
		}
		return series;
	}
	
//	public void addInterVallMarker(IntervalMarker m){
//		plot.addDomainMarker(m, Layer.BACKGROUND);
//	}

	public XYItemRenderer getRender() {
		return render;
	}

	public void setRender(XYItemRenderer render) {
		this.render = render;
	}

	public void setSlice(int slice) {
//		System.out.println("slice set");
		marker.setValue(slice);
	}

}
