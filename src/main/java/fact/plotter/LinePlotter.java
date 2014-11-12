package fact.plotter;

import java.io.Serializable;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import fact.Utils;
import stream.Data;
import stream.ProcessContext;
import stream.plotter.DataVisualizer;

public class LinePlotter extends DataVisualizer
{
	private String xValue = "";
	private String yValue = "";

	private String title = "Default Titel";

	private XYSeriesCollection dataset;
	private XYSeries series;

	private void showGraph()
	{
		final JFreeChart chart = ChartFactory.createXYLineChart(title, "X", "Y", dataset);

		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(640, 480));
		final ApplicationFrame frame = new ApplicationFrame(title);
		frame.setContentPane(chartPanel);
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void init(ProcessContext ctx) throws Exception
	{
		dataset = new XYSeriesCollection();
		series = new XYSeries("data", false);
		dataset.addSeries(series);
		showGraph();
		super.init(ctx);
	}

	@Override
	public Data processMatchingData(Data data)
	{
		series.clear();
		if( data.containsKey(xValue) && data.containsKey(yValue) )
		{
			Serializable[] ox = (Serializable[]) data.get(xValue);
			Serializable[] oy = (Serializable[]) data.get(yValue);

			if(ox == null || oy == null) return data;
			
			for(int i = 0; i < Math.min(ox.length, oy.length); i++)
			{
				if( ox[i] == null )
					continue;
				
				double dx = Utils.valueToDouble(ox[i]);
				double dy = 0;
				
				if( oy[i] != null )				
					dy = Utils.valueToDouble(oy[i]);
					
				

				series.add(dx, dy);
			}
		}

		return data;
	}

	@Override
	public void finish() throws Exception
	{

	}

	public String getxValue()
	{
		return xValue;
	}

	public void setxValue(String xValue)
	{
		this.xValue = xValue;
	}

	public String getyValue()
	{
		return yValue;
	}

	public void setyValue(String yValue)
	{
		this.yValue = yValue;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

}
