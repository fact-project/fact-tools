package fact.calibrationservice;

import java.util.TreeSet;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.auxservice.AuxFileService;
import fact.hexmap.FactPixelMapping;

public class ConstantCalibService implements CalibrationService {
	
	Logger log = LoggerFactory.getLogger(AuxFileService.class);
	
	FactPixelMapping pixelMap = FactPixelMapping.getInstance();
	
	TreeSet<HardwareConfiguration> set;
	
	/*
	 * From the beginning on there were
	 * 3 dead pixels:
	 * CHIDs: 927,80,873
	 * 3 crazy pixels:
	 * CHIDs: 863,297,868
	 * 6 twin pixels:
	 * CHIDs: 1093,1094,527,528,721,722
	 * 
	 * All this bad pixels can be interpolated and are all usable for cleaning (so there are no
	 * NotUsablePixels
	 * 
	 * During the period between 2014/11/15 and 2015/05/26 there was a drs board broken:
	 * SOFTIDs: 1193,1194,1195,1391,1392,1393,1304,1305,1306 
	 * The whole patch cannot be interpolated and therefore all pixels are not usable for cleaning.
	 */
	
	
	public ConstantCalibService(){
		int[] badPixels = {863,868,297,927,80,873,1093,1094,527,528,721,722};
		HardwareConfiguration config1 = new HardwareConfiguration(new DateTime(1970, 1, 1, 0, 0));
		config1.setBadPixels(badPixels);
		config1.setNotUsablePixels(null);
		int[] brokenDrsBoard = {
				pixelMap.getChidFromSoftID(1193),
				pixelMap.getChidFromSoftID(1194),
				pixelMap.getChidFromSoftID(1195),
				pixelMap.getChidFromSoftID(1391),
				pixelMap.getChidFromSoftID(1392),
				pixelMap.getChidFromSoftID(1393),
				pixelMap.getChidFromSoftID(1304),
				pixelMap.getChidFromSoftID(1305),
				pixelMap.getChidFromSoftID(1306),
				};
		HardwareConfiguration config2 = new HardwareConfiguration(new DateTime(2014, 11, 15, 0, 0));
		config2.setBadPixels(badPixels);
		config2.setNotUsablePixels(brokenDrsBoard);
		HardwareConfiguration config3 = new HardwareConfiguration(new DateTime(2015, 5, 26, 0, 0));
		config3.setBadPixels(badPixels);
		config3.setNotUsablePixels(null);
		set.add(config1);
		set.add(config2);
		set.add(config3);
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
