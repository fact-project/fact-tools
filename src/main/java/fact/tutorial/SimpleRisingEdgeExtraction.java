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

	@Parameter(required = false, description = "key to the data array")
	protected String dataKey = "DataCalibrated";
	@Parameter(required = false, description = "outputKey for the rising edge")
	protected String outputKey = "RisingEdge";

	@Override
	public Data process(Data input)
	{

		int numberOfPixel = 1440;
		int roi = 300;
		IntervalMarker[] marker = new IntervalMarker[numberOfPixel];

		double[] data = (double[]) input.get(dataKey);

		double[] arrivalTime = new double[numberOfPixel];
		double[] derivative;

		for (int pix = 0; pix < numberOfPixel; pix++)
		{
			derivative = differentiateData(pix, roi, data);
			arrivalTime[pix] = argMax(25, 100, derivative);
			marker[pix] = new IntervalMarker(arrivalTime[pix] - 0.2, arrivalTime[pix] + 0.2);
		}

		input.put(outputKey, arrivalTime);
		input.put(outputKey + "_marker", marker);

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
