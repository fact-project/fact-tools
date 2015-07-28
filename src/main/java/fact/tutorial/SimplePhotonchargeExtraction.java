package fact.tutorial;

import fact.Utils;
import org.jfree.chart.plot.IntervalMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 *  *
 * @author Maximilian Noethe
 *
 */
public class SimplePhotonchargeExtraction implements Processor
{
	static Logger log = LoggerFactory.getLogger(SimplePhotonchargeExtraction.class);

	@Parameter(required = false, description = "key to the data array")
	protected String dataKey = "DataCalibrated";
	@Parameter(required = false, description = "inputKey for the risingEdge")
	protected String risingEdgeKey = "RisingEdge";
	@Parameter(required = false, description = "outputKey for the rising edge")
	protected String outputKey = "photons";


	@Override
	public Data process(Data input)
	{

		Utils.mapContainsKeys(input, dataKey, "NROI");
		int roi = (Integer) input.get("NROI");
		int npix = (Integer) input.get("NPIX");
		IntervalMarker[] marker = new IntervalMarker[npix];

		double[] data = (double[]) input.get(dataKey);
		double[] risingEdge = (double[]) input.get(risingEdgeKey);
		double[] photoncharge = new double[npix];

		for (int pix = 0; pix < npix; pix++)
		{
			photoncharge[pix] = 0;
			int start = (int) risingEdge[pix] + pix * roi;
			for (int slice = start; slice < start + 30; slice++)
			{
				photoncharge[pix] += data[slice] / 230.0;
			}
			marker[pix] = new IntervalMarker(risingEdge[pix], risingEdge[pix] + 30);
		}

		input.put(outputKey, photoncharge);
		input.put(outputKey + "Marker", marker);

		return input;
	}
	


	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}


	public void setRisingEdgeKey(String risingEdgeKey) {
		this.risingEdgeKey = risingEdgeKey;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

}
