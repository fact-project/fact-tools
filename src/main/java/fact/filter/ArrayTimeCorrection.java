package fact.filter;

import fact.Utils;
import fact.utils.LinearTimeCorrectionKernel;
import fact.utils.TimeCorrectionKernel;
import stream.Data;
import stream.Processor;
import stream.ProcessorException;
import stream.annotations.Parameter;

//TODO write unit test
public class ArrayTimeCorrection implements Processor{
		
	private String dataKey = null;
	private double[] data = null;
	
	private String timeCalibConstKey = null;
	private double[] timeCalibConst = null;

	private String outputKey = null;
	
	private int npix;
	private int roi = 0;
	private TimeCorrectionKernel tcKernel = null;
	
	@Override
	public Data process(Data input) {		
		Utils.isKeyValid(input, "NPIX", Integer.class);
		Utils.mapContainsKeys( input, dataKey, timeCalibConstKey);
		npix = (Integer) input.get("NPIX");
		data = (double[]) input.get(dataKey);
		roi = data.length / npix;
		timeCalibConst = (double[]) input.get(timeCalibConstKey);
		tcKernel = new LinearTimeCorrectionKernel();		

		double [] calibratedValues = new double[roi * npix];
		
		for(int id = 0; id < npix; id++)
		{
			double [] realtimes = new double[roi];
			double [] values = new double[roi];
			
			for(int slice = 0; slice < roi; slice++)
			{
				realtimes[slice] = getTime(id, slice);
				values[slice] = data[id * roi + slice];
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
	
	

	public String getDataKey() {
		return dataKey;
	}



	public void setDataKey(String dataKey) {
		this.dataKey = dataKey;
	}



	public String getTimeCalibConstKey() {
		return timeCalibConstKey;
	}



	public void setTimeCalibConstKey(String timeCalibConstKey) {
		this.timeCalibConstKey = timeCalibConstKey;
	}



	public String getOutputKey() {
		return outputKey;
	}

	@Parameter(required = true, description = "Outputkey", defaultValue = "timeCalibratedData")
	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	/**
	 * 
	 * @param chid
	 * @param slice
	 * @return time in ns!
	 */
	private double getTime(int chid, int slice){
		return 0.5 * (double) (slice - timeCalibConst[chid * roi + slice]);
	}
	

	
}
