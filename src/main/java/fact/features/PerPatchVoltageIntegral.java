package fact.features;

import fact.Constants;
import fact.EventUtils;
import fact.viewer.ui.DefaultPixelMapping;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * This processor calculates the per patch voltage integral for trigger simulation.
 */

public class PerPatchVoltageIntegral implements Processor {

	private double[] dataCalibratedArray = null;
	private String key = "";
	
	
	public String getKey() {
		return key;
	}
	@Parameter(required = true, defaultValue = "dataCalibrated", description = "The input key")
	public void setKey(String key) {
		this.key = key;
	}

	public String getOutputKey() {
		return outputKey;
	}

	@Parameter(required = true, defaultValue = "perPatchVoltageIntegral", description = "The output key")
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	private String outputKey = "";
	private double[] perPatchVoltageIntegral = null;
	private int roi = 0;
	
	@Override
	public Data process(Data input) {
		
		EventUtils.mapContainsKeys(getClass(), input, key);
		dataCalibratedArray = (double[]) input.get(key);
		roi = dataCalibratedArray.length / Constants.NUMBEROFPIXEL;
		perPatchVoltageIntegral = new double[160];
		
		int patch = 0;

		for(int chid = 0; chid < Constants.NUMBEROFPIXEL; chid++)
		{
			for(int slice = 0; slice < roi; slice++)
			{
				patch = DefaultPixelMapping.getPatch(chid);
				perPatchVoltageIntegral[patch] += 0.5 * dataCalibratedArray[chid * roi + slice]; // 0.5 ns per slice
			}
		}
		
		input.put(outputKey, perPatchVoltageIntegral);

		return input;
	}

}
