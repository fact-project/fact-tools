package fact.calibrationservice;

import fact.container.PixelSet;
import fact.hexmap.FactPixelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TreeSet;

/**
 *
 * See https://trac.fact-project.org/wiki/Protected/KnownProblems
 *
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

    public static final PixelSet deadPixels = PixelSet.fromIDs(new int[]{80, 873, 927});
    public static final PixelSet crazyPixels = PixelSet.fromIDs(new int[]{297, 863, 868});
    public static final PixelSet twinPixels = PixelSet.fromIDs(new int[]{527, 528, 721, 722, 1093, 1094});

    public static final PixelSet brokenDrsBoard20142015 = PixelSet.fromIDs(new int[]{720, 721, 722, 723, 724, 725, 726, 727, 728});
    public static final PixelSet biasPatch38 = PixelSet.fromIDs(new int[] {171, 172, 173, 174});
    public static final PixelSet biasPatch272 = PixelSet.fromIDs(new int[] {1296, 1297, 1298, 1299});

    public void init() {
        set = new TreeSet<>();

        PixelSet badPixelsFromBeginning = new PixelSet();
        badPixelsFromBeginning.addAll(deadPixels);
        badPixelsFromBeginning.addAll(crazyPixels);
        badPixelsFromBeginning.addAll(twinPixels);

        // Hardware config at beginning of observations, also used for all current simulations
        HardwareConfiguration configStart = new HardwareConfiguration(ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        configStart.badPixels.addAll(badPixelsFromBeginning);

        // One DRSBoard was broken from 2014-11-15 until 2015-05-26
        HardwareConfiguration config20141115 = new HardwareConfiguration(ZonedDateTime.of(2014, 11, 15, 12, 0, 0, 0, ZoneOffset.UTC));
        config20141115.badPixels.addAll(badPixelsFromBeginning);
        config20141115.notUsablePixels.addAll(brokenDrsBoard20142015);

        //  in the night of 2015-01-08, two pixels were broken additionally (729 & 750)
        HardwareConfiguration config20150108 = new HardwareConfiguration(ZonedDateTime.of(2015, 1, 8, 12, 0, 0, 0, ZoneOffset.UTC));
        config20150108.badPixels.addAll(badPixelsFromBeginning);
        config20150108.badPixels.addById(729);
        config20150108.badPixels.addById(750);
        config20150108.notUsablePixels.addAll(brokenDrsBoard20142015);

        // On 2015-01-09, pixel 729 recovered
        HardwareConfiguration config20150109 = new HardwareConfiguration(ZonedDateTime.of(2015, 1, 9, 12, 0, 0, 0, ZoneOffset.UTC));
        config20150109.badPixels.addAll(badPixelsFromBeginning);
        config20150109.badPixels.addById(750);
        config20150109.notUsablePixels.addAll(brokenDrsBoard20142015);

        // On 2015-01-31, pixel 750 recovered
        HardwareConfiguration config20150131 = new HardwareConfiguration(ZonedDateTime.of(2015, 1, 31, 12, 0, 0, 0, ZoneOffset.UTC));
        config20150131.badPixels.addAll(badPixelsFromBeginning);
        config20150131.notUsablePixels.addAll(brokenDrsBoard20142015);

        // bias boards behaving strange
        HardwareConfiguration config20150206 = new HardwareConfiguration(ZonedDateTime.of(2015, 2, 6, 12, 0, 0, 0, ZoneOffset.UTC));
        config20150206.badPixels.addAll(badPixelsFromBeginning);
        config20150206.badPixels.addAll(biasPatch38);
        config20150206.notUsablePixels.addAll(brokenDrsBoard20142015);

        HardwareConfiguration config20150212 = new HardwareConfiguration(ZonedDateTime.of(2015, 2, 12, 12, 0, 0, 0, ZoneOffset.of("+00:00")));
        config20150212.badPixels.addAll(badPixelsFromBeginning);
        config20150212.notUsablePixels.addAll(brokenDrsBoard20142015);

        HardwareConfiguration config20150216 = new HardwareConfiguration(ZonedDateTime.of(2015, 2, 16, 12, 0, 0, 0, ZoneOffset.of("+00:00")));
        config20150216.badPixels.addAll(badPixelsFromBeginning);
        config20150216.badPixels.addAll(biasPatch38);
        config20150216.notUsablePixels.addAll(brokenDrsBoard20142015);

        // broken drs board replaced
        HardwareConfiguration config20150526 = new HardwareConfiguration(ZonedDateTime.of(2015, 5, 26, 0, 0, 0, 0, ZoneOffset.of("+00:00")));
        config20150526.badPixels.addAll(badPixelsFromBeginning);
        config20150526.badPixels.addAll(biasPatch38);

        HardwareConfiguration config20160313 = new HardwareConfiguration(ZonedDateTime.of(2016, 3, 13, 12, 0, 0, 0, ZoneOffset.of("+00:00")));
        config20160313.badPixels.addAll(badPixelsFromBeginning);
        config20160313.badPixels.addAll(biasPatch38);
        config20160313.badPixels.addAll(biasPatch272);

        set.add(configStart);
        set.add(config20141115);
        set.add(config20150108);
        set.add(config20150109);
        set.add(config20150131);
        set.add(config20150206);
        set.add(config20150212);
        set.add(config20150216);
        set.add(config20150526);
        set.add(config20160313);
    }

    /**
     * @see CalibrationService#getBadPixels(ZonedDateTime)
     */
    @Override
    public PixelSet getBadPixels(ZonedDateTime eventTimeStamp) {
        if (isInit == false) {
            init();
            isInit = true;
        }
        HardwareConfiguration currentConfiguration = getHardwareConfiguration(eventTimeStamp);
        return currentConfiguration.badPixels;
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
        return currentConfiguration.notUsablePixels;
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
