package fact.features;

import fact.Constants;
import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This processor calculates the arrival time distribution per patch for trigger simulation.
 * @author jan
 *
 */
public class PerPatchArrivalTimeDistribution implements Processor {
	
	String key;
	public String getKey() {
		return key;
	}

	@Parameter(required=true, description="Key to an arrivaltime array.", defaultValue="arrivalTime")
	public void setKey(String key) {
		this.key = key;
	}

	public String getOutputKey() {
		return outputKey;
	}

	@Parameter(required = true, description = "Outputkey", defaultValue="perPatchArrivalTime")
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	String outputKey;

	@Override
	public Data process(Data input) {
		
		Utils.mapContainsKeys( input, key);
		
		double[] arrivalTimeArray = Utils.toDoubleArray(input.get(key));
		//try{
		double[] perPatchMean = new double[160];
		double[] perPatchVariance = new double[160];

		int patch = 0;
		for(int chid = 0; chid < Constants.NUMBEROFPIXEL; chid++)
		{

			patch = (int) chid/9;
			perPatchMean[patch] += arrivalTimeArray[chid] / 9.0;
		}
		for(int chid = 0; chid < Constants.NUMBEROFPIXEL; chid++)
		{
			patch = chid/9;
			perPatchVariance[patch] += (arrivalTimeArray[chid] - perPatchMean[patch]) * (arrivalTimeArray[chid] - perPatchMean[patch]) / 8.0;
		}

		input.put(outputKey + "_mean", perPatchMean);
		input.put(outputKey + "_var", perPatchVariance);
		/*}catch(Exception e)
		{
			input.put(outputKey + "_mean", null);
			input.put(outputKey + "_var", null);
			return input;
		}*/
		
		return input;
	}

}
