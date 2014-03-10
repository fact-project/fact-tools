package fact.filter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.Processor;
import stream.ProcessorException;
import stream.annotations.Parameter;
import fact.Constants;
import fact.EventUtils;
import fact.utils.LinearTimeCorrectionKernel;
import fact.utils.TimeCorrectionKernel;

//TODO write unit test
public class ArrayTimeCorrection implements Processor{

	@Override
	public Data process(Data input) {
		
		EventUtils.mapContainsKeys(getClass(), input,  kernel, dataCalibrated, timesOffset);
		
		try{
			dataCalibratedArray = (double[]) input.get(dataCalibrated);
			roi = dataCalibratedArray.length / Constants.NUMBEROFPIXEL;
			timesOffsetArray = (double[]) input.get(timesOffset);
			String kernelStr = (String) input.get(kernel);
			if(kernelStr.equalsIgnoreCase("linear"))
				tcKernel = new LinearTimeCorrectionKernel();
			else
				throw new ProcessorException("Right now there is just the linear kernel.");
				
		}catch(Exception e)
		{
			throw new ProcessorException(e.getMessage() + " or something went terribly wrong in ArrayTimeCorrection. The keys were not readable.");
		}
		double [] calibratedValues = new double[roi * Constants.NUMBEROFPIXEL];
		
		for(int id = 0; id < Constants.NUMBEROFPIXEL; id++)
		{
			double [] realtimes = new double[roi];
			double [] values = new double[roi];
			
			for(int slice = 0; slice < roi; slice++)
			{
				realtimes[slice] = getTime(id, slice);
				values[slice] = dataCalibratedArray[id * roi + slice];
			}
			tcKernel.fit(realtimes, values);
			
			for(int slice = 0; slice < roi; slice++)
			{
				calibratedValues[id * roi + slice] = tcKernel.interpolate((double) slice * 0.5);
			}
			
		}
		
		input.put(outputKey, calibratedValues);
		
		
		return input;
	}
	
	public String getKernel() {
		return kernel;
	}
	@Parameter(required = true, description = "The kernel, that is use for rebinning.", defaultValue = "linear")
	public void setKernel(String kernel) {
		this.kernel = kernel;
	}

	public String getDataCalibrated() {
		return dataCalibrated;
	}
	@Parameter(required = true, description = "dataCalibrated", defaultValue = "dataCalibrated")
	public void setDataCalibrated(String dataCalibrated) {
		this.dataCalibrated = dataCalibrated;
	}

	public String getTimesOffset() {
		return timesOffset;
	}
	
	@Parameter(required = true, description = "The array from DrsTimeCalibration.", defaultValue = "timeOffset")
	public void setTimesOffset(String timesOffset) {
		this.timesOffset = timesOffset;
	}

	public String getOutputKey() {
		return outputKey;
	}

	@Parameter(required = true, description = "Outputkey", defaultValue = "timeCalibratedData")
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	private double getTime(int chid, int slice){
		return 0.5 * (double) slice + timesOffsetArray[chid * roi + slice];
	}
	
	private String kernel = null;
	private TimeCorrectionKernel tcKernel = null;
	
	private String dataCalibrated = null;
	private double[] dataCalibratedArray = null;
	
	private String timesOffset = null;
	private double[] timesOffsetArray = null;

	private String outputKey = null;
	private int roi = 0;
	
}
