package fact.filter;

import fact.Utils;
import fact.utils.LinearTimeCorrectionKernel;
import fact.utils.TimeCorrectionKernel;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

//TODO write unit test
public class ArrayTimeCorrection implements Processor{

	@Parameter(required = false, defaultValue = "raw:dataCalibrated",
			description = "key to the drs amplitude calibrated voltage curves")
	private String inputKey = "raw:dataCalibrated";
	@Parameter(required = false, defaultValue = "meta:timeCalibConst",
			description = "Key to the time calibration constants as calculated by fact.filter.DrsTimeCalibration")
	private String timeCalibConstKey = "meta:timeCalibConst";
	@Parameter(required = false, defaultValue = "raw:dataCalibrated",
			description = "OutputKey for the calibrated voltage curves")
	private String outputKey = "raw:dataCalibrated";

	private double[] data = null;
	private double[] timeCalibConst = null;

	private int npix;
	private int roi = 0;
	private TimeCorrectionKernel tcKernel = null;
	
	@Override
	public Data process(Data item) {
		Utils.isKeyValid(item, "NPIX", Integer.class);
		Utils.mapContainsKeys( item, inputKey, timeCalibConstKey);
		npix = (Integer) item.get("NPIX");
		data = (double[]) item.get(inputKey);
		roi = data.length / npix;
		timeCalibConst = (double[]) item.get(timeCalibConstKey);
		tcKernel = new LinearTimeCorrectionKernel();		

		double [] calibratedValues = new double[roi * npix];
		
		for(int id = 0; id < npix; id++)
		{
			double [] realtimes = new double[roi];
			double [] values = new double[roi];
			
			for(int slice = 0; slice < roi; slice++)
			{
				realtimes[slice] = calcRealTime(id, slice);
				values[slice] = data[id * roi + slice];
			}
			tcKernel.fit(realtimes, values);
			
			for(int slice = 0; slice < roi; slice++)
			{
				calibratedValues[id * roi + slice] = tcKernel.interpolate((double) slice);
			}
			
		}
		
		item.put(outputKey, calibratedValues);
		return item;
	}


	/**
	 *
	 * @param chid continous hardware id of the pixel
	 * @param slice slice in the time series
	 * @return time in slices
	 */
	private double calcRealTime(int chid, int slice){
		return slice - timeCalibConst[chid * roi + slice];
	}
}
