package fact.features;

import fact.Constants;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class VoltageSumPerSlice implements Processor {
	
	public String key = "";
	public String outputKey = "";
	
	
	@Override
	public Data process(Data input) {

		double [] dataCalibrated = (double[]) input.get(key);
		int roi = dataCalibrated.length / Constants.NUMBEROFPIXEL;
		double [] voltageSumPerSlice = new double[roi];
		for(int slice = 0; slice < roi; slice++){
			for(int chid = 0; chid < Constants.NUMBEROFPIXEL; chid++){
				voltageSumPerSlice[slice] += dataCalibrated[roi * chid + slice];
			}
		}
		input.put(outputKey, voltageSumPerSlice);
		return input;
	}


	public String getKey() {
		return key;
	}

	@Parameter(required = true, description ="Key", defaultValue="dataCalibrated")
	public void setKey(String key) {
		this.key = key;
	}


	public String getOutputKey() {
		return outputKey;
	}

	@Parameter(required = true, description="Outputkey", defaultValue="voltageSumPerSlice")
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

}
