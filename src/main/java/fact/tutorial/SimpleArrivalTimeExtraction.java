package fact.tutorial;

import fact.Constants;
import fact.Utils;
import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;
import stream.io.CsvStream;
import stream.io.SourceURL;

/**
 * This processor performs a basic extraction on the data array. It contains three steps:
 * 1. Calculates the position of the max amplitude in [startSearchWindow,startSearchWindow+rangeSearchWindow[
 * 2. Calculates the position of the half height in front of the maxAmplitudePosition
 * 3. Calculates the integral by summing up the following integrationWindow slices beginning with the half heigth position
 * The resulting photoncharge is calculated by dividing the integral by the integralGain of the pixel
 * 
 * This processor also serves as a basic class for extraction processors
 * 
 * @author Fabian Temme
 *
 */
public class SimpleArrivalTimeExtraction implements Processor
{
	static Logger log = LoggerFactory.getLogger(SimpleArrivalTimeExtraction.class);

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

		int[] positions = new int[npix];

		double[] arrivalTime = new double[npix];
		double[] derivative = new double[roi];

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


	public String getDataKey() {
		return dataKey;
	}

	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}

	public String getOutputKey() {
		return outputKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

}
