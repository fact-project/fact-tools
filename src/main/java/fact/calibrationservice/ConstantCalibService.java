package fact.calibrationservice;

import java.util.TreeSet;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.auxservice.AuxFileService;

public class ConstantCalibService implements CalibrationService {
	
	Logger log = LoggerFactory.getLogger(AuxFileService.class);
	
	TreeSet<HardwareConfiguration> set;
	
	public ConstantCalibService(){
		int[] badPixels = {863,868,297,927,80,873,1093,1094,527,528,721,722};
		HardwareConfiguration config1 = new HardwareConfiguration(new DateTime(1970, 1, 1, 0, 0));
		config1.setBadPixels(badPixels);
		config1.setNotUsablePixels(null);
		set.add(config1);
	}

	@Override
	public int[] getBadPixel(DateTime eventTimeStamp) {
		HardwareConfiguration currentConfiguration = getHardwareConfiguration(eventTimeStamp);
		return currentConfiguration.getBadPixels();
	}

	@Override
	public int[] getNotUsablePixels(DateTime eventTimeStamp) {
		HardwareConfiguration currentConfiguration = getHardwareConfiguration(eventTimeStamp);
		return currentConfiguration.getNotUsablePixels();
	}
	
	private HardwareConfiguration getHardwareConfiguration(DateTime eventTimeStamp) {
		HardwareConfiguration dummyConfiguration = new HardwareConfiguration(eventTimeStamp);
		return set.floor(dummyConfiguration);
	}
	
	@Override
	public void reset() throws Exception {
	}
	
}
