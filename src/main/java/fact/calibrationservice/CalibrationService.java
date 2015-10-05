package fact.calibrationservice;

import org.joda.time.DateTime;

import stream.service.Service;

public interface CalibrationService extends Service {
	
	public int[] getBadPixel(DateTime eventTimeStamp);
	
	public int[] getNotUsablePixels(DateTime eventTimeStamp);

}
