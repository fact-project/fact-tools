package fact.calibrationservice;

import fact.container.PixelSet;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TreeSet;

/**
 * From the beginning on there were
 * 3 dead pixels:
 * CHIDs: 927,80,873
 * 3 crazy pixels:
 * CHIDs: 863,297,868
 * 6 twin pixels:
 * CHIDs: 1093,1094,527,528,721,722
 * <p>
 * All this bad pixels can be interpolated and are all usable for cleaning (so there are no
 * NotUsablePixels)
 * <p>
 * During the period between 2014/11/15 and 2015/05/26 there was a drs board broken:
 * SOFTIDs: 1193,1194,1195,1391,1392,1393,1304,1305,1306
 * CHIDs:    720, 721, 722, 723, 724, 725, 726, 727, 728 (not the same order as the SOFTIDs!)
 * The whole patch cannot be interpolated and therefore all pixels are not usable for cleaning.
 * <p>
 * On the 8.1.2015 two more pixels showed no signal and had to be interpolated:
 * CHIDs: 729,750
 * <p>
 * On the 9.1.2015 only one more pixel showed no signal and had to be interpolated:
 * CHID: 750
 * <p>
 * On the 31.1.2015 pixel (CHID 750) recovered.
 * <p>
 * With beginning of February 2015 two bias patches got some problems:
 * The bias patch for the pixels (CHIDs: 171,172,173,174) were dead (so the pixels showed no signal)
 * between 6.2. and 11.2 (included) and from 16.2. (included) until now
 * <p>
 * There were also some days (12.2. - 14.2.) were the pixels showed two small signals (so maybe
 * the bias voltage was too low), but on the other hand on the 15.2. they show normal signals
 * <p>
 * The bias patch for the pixels (CHIDs: 184,185,186,187,188) showed sometimes also a lowered bias voltage
 * (so signals were smaller) but no clear timewindow could be found
 * <p>
 * Concerning the bad pixel list, the days with only lowered bias voltage are not interpolated, for the days
 * with no bias voltage the pixels are interpolated.
 *
 * @author ftemme
 **/
public class ConstantCalibService implements CalibrationService {

    Logger log = LoggerFactory.getLogger(ConstantCalibService.class);

    TreeSet<HardwareConfiguration> set;

    boolean isInit = false;

    public void init() {
        FactPixelMapping pixelMap = FactPixelMapping.getInstance();
        set = new TreeSet<>();


        HardwareConfiguration config1 = new HardwareConfiguration(ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        PixelSet badPixelFromBeginning = PixelSet.fromIDs(new int[]{863, 868, 297, 927, 80, 873, 1093, 1094, 527, 528, 721, 722});
        config1.setBadPixels(badPixelFromBeginning);
        config1.setNotUsablePixels(new PixelSet());

        HardwareConfiguration config2 = new HardwareConfiguration(ZonedDateTime.of(2014, 11, 15, 0, 0, 0, 0, ZoneOffset.UTC));
        PixelSet brokenDrsBoard = PixelSet.fromIDs(new int[]{
                pixelMap.getChidFromSoftID(1193),
                pixelMap.getChidFromSoftID(1194),
                pixelMap.getChidFromSoftID(1195),
                pixelMap.getChidFromSoftID(1391),
                pixelMap.getChidFromSoftID(1392),
                pixelMap.getChidFromSoftID(1393),
                pixelMap.getChidFromSoftID(1304),
                pixelMap.getChidFromSoftID(1305),
                pixelMap.getChidFromSoftID(1306),
        });
        config2.setBadPixels(badPixelFromBeginning);
        config2.setNotUsablePixels(brokenDrsBoard);

        HardwareConfiguration config3 = new HardwareConfiguration(ZonedDateTime.of(2015, 01, 8, 0, 0, 0, 0, ZoneOffset.UTC));
        int[] badPixelInJanuary1 = {863, 868, 297, 927, 80, 873, 1093, 1094, 527, 528, 721, 722, 750, 729};
        config3.setBadPixels(PixelSet.fromIDs(badPixelInJanuary1));
        config3.setNotUsablePixels(brokenDrsBoard);

        HardwareConfiguration config4 = new HardwareConfiguration(ZonedDateTime.of(2015, 01, 9, 0, 0, 0, 0, ZoneOffset.UTC));
        int[] badPixelInJanuary2 = {863, 868, 297, 927, 80, 873, 1093, 1094, 527, 528, 721, 722, 750};
        config4.setBadPixels(PixelSet.fromIDs(badPixelInJanuary2));
        config4.setNotUsablePixels(brokenDrsBoard);

        HardwareConfiguration config5 = new HardwareConfiguration(ZonedDateTime.of(2015, 01, 31, 0, 0, 0, 0, ZoneOffset.UTC));
        config5.setBadPixels(badPixelFromBeginning);
        config5.setNotUsablePixels(brokenDrsBoard);

        HardwareConfiguration config6 = new HardwareConfiguration(ZonedDateTime.of(2015, 2, 6, 0, 0, 0, 0, ZoneOffset.UTC));
        PixelSet badPixelWithDeadBiasBord = PixelSet.fromIDs(new int[]{863, 868, 297, 927, 80, 873, 1093, 1094, 527, 528, 721, 722, 171, 172, 173, 174});
        config6.setBadPixels(badPixelWithDeadBiasBord);
        config6.setNotUsablePixels(brokenDrsBoard);

        HardwareConfiguration config7 = new HardwareConfiguration(ZonedDateTime.of(2015, 2, 12, 0, 0, 0, 0, ZoneOffset.of("+00:00")));
        config7.setBadPixels(badPixelFromBeginning);
        config7.setNotUsablePixels(brokenDrsBoard);

        HardwareConfiguration config8 = new HardwareConfiguration(ZonedDateTime.of(2015, 2, 16, 0, 0, 0, 0, ZoneOffset.of("+00:00")));
        config8.setBadPixels(badPixelWithDeadBiasBord);
        config8.setNotUsablePixels(brokenDrsBoard);

        HardwareConfiguration config9 = new HardwareConfiguration(ZonedDateTime.of(2015, 5, 26, 0, 0, 0, 0, ZoneOffset.of("+00:00")));
        config9.setBadPixels(badPixelWithDeadBiasBord);
        config9.setNotUsablePixels(new PixelSet());
        set.add(config1);
        set.add(config2);
        set.add(config3);
        set.add(config4);
        set.add(config5);
        set.add(config6);
        set.add(config7);
        set.add(config8);
        set.add(config9);
    }

    /**
     * @see CalibrationService#getBadPixel(ZonedDateTime)
     */
    @Override
    public PixelSet getBadPixel(ZonedDateTime eventTimeStamp) {
        if (isInit == false) {
            init();
            isInit = true;
        }
        HardwareConfiguration currentConfiguration = getHardwareConfiguration(eventTimeStamp);
        return currentConfiguration.getBadPixels();
    }

    /**
     * @see CalibrationService#getNotUsablePixels(ZonedDateTime)
     */
    @Override
    public PixelSet getNotUsablePixels(ZonedDateTime eventTimeStamp) {
        if (isInit == false) {
            init();
            isInit = true;
        }
        HardwareConfiguration currentConfiguration = getHardwareConfiguration(eventTimeStamp);
        return currentConfiguration.getNotUsablePixels();
    }

    private HardwareConfiguration getHardwareConfiguration(ZonedDateTime eventTimeStamp) {
        // Create a new dummyConfiguration with the current eventTimeStamp and use the set.floor method to get the correct configuration
        HardwareConfiguration dummyConfiguration = new HardwareConfiguration(eventTimeStamp);
        return set.floor(dummyConfiguration);
    }

    @Override
    public void reset() throws Exception {
    }

}
