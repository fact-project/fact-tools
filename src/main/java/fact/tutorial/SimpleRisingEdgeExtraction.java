package fact.tutorial;

import fact.Utils;
import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 *
 * @author Maximilian Noethe
 *
 */
public class SimpleRisingEdgeExtraction implements Processor
{
	static Logger log = LoggerFactory.getLogger(SimpleRisingEdgeExtraction.class);

	@Parameter(required = true, description = "key to the data array")
	protected String dataKey = null;
	@Parameter(required = true, description = "outputKey for the rising edge")
	protected String outputKey = null;

	@Override
	public Data process(Data input)
	{

		Utils.mapContainsKeys(input, dataKey, "NROI");
		int roi = (Integer) input.get("NROI");
		int npix = (Integer) input.get("NPIX");
		IntervalMarker[] marker = new IntervalMarker[npix];

		double[] data = (double[]) input.get(dataKey);

		double[] arrivalTime = new double[npix];
		double[] derivative;

		for (int pix = 0; pix < npix; pix++)
		{
			derivative = differentiateData(pix, roi, data);
			arrivalTime[pix] = argMax(25, 100, derivative);
			marker[pix] = new IntervalMarker(arrivalTime[pix] - 0.2, arrivalTime[pix] + 0.2);
		}

		input.put(outputKey, arrivalTime);
		input.put(outputKey + "Marker", marker);

		return input;
	}

	public double[] differentiateData(int pixel, int roi, double[] data)
	{
		double[] derivative = new double[roi];
		for (int slice = 0; slice < roi; slice++)
		{
			if (slice > 0 && slice < roi - 1)
			{
				derivative[slice] = (data[pixel * roi + slice + 1] - data[pixel * roi + slice - 1]) / 2.0;
			}
			else
			{
				derivative[slice] = 0;
			}
		}
		return derivative;
	}


	public int argMax(int start, int rightBorder, double[] data)
	{
		int maxPos = start;
		double tempMax = -Double.MAX_VALUE;
		for (int slice = start; slice < rightBorder; slice++)
		{
			if (data[slice] > tempMax)
			{
				maxPos = slice;
				tempMax = data[slice];
			}
		}
		return maxPos;
	}



	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}


	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

}
