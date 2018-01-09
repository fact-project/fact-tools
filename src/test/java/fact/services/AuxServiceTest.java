package fact.services;


import fact.auxservice.AuxCache;
import fact.auxservice.AuxFileService;
import fact.auxservice.AuxPoint;
import fact.auxservice.AuxiliaryServiceName;
import fact.auxservice.strategies.Closest;
import org.junit.Test;
import stream.io.SourceURL;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.SortedSet;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.*;


/**
 * Test some things about the AuxFileService
 * Created by kaibrugge on 17.11.14.
 */
public class AuxServiceTest {


    @Test
    public void testDateTimeToFACTNight() {
        Integer factNight = AuxCache.dateTimeStampToFACTNight(ZonedDateTime.parse("2014-01-02T23:55:02Z"));
        assertThat(factNight, is(20140102));

        //now test what happens after 00:00
        factNight = AuxCache.dateTimeStampToFACTNight(ZonedDateTime.parse("2014-01-03T00:55:02Z"));
        assertThat(factNight, is(20140102));

        //now test the next day
        factNight = AuxCache.dateTimeStampToFACTNight(ZonedDateTime.parse("2014-01-03T12:00:01Z"));
        assertThat(factNight, is(20140103));

        //now test what happens after newyears
        factNight = AuxCache.dateTimeStampToFACTNight(ZonedDateTime.parse("2014-01-01T00:55:02Z"));
        assertThat(factNight, is(20131231));

        //now test my birfday!!
        factNight = AuxCache.dateTimeStampToFACTNight(ZonedDateTime.parse("1987-09-20T20:55:02Z"));
        assertThat(factNight, is(19870920));
    }


    @Test
    public void testDateTimeToFACTPath() {
        Path factNight = AuxCache.dateTimeStampToFACTPath(ZonedDateTime.parse("2014-01-02T23:55:02Z"));

        assertThat(factNight, is(Paths.get("2014", "01", "02")));

        //now test what happens after 00:00
        factNight = AuxCache.dateTimeStampToFACTPath(ZonedDateTime.parse("2014-01-03T00:55:02Z"));
        assertThat(factNight, is(Paths.get("2014", "01", "02")));

        //now test the next day
        factNight = AuxCache.dateTimeStampToFACTPath(ZonedDateTime.parse("2014-01-03T12:00:01Z"));
        assertThat(factNight, is(Paths.get("2014", "01", "03")));

        //now test what happens after newyears
        factNight = AuxCache.dateTimeStampToFACTPath(ZonedDateTime.parse("2014-01-01T00:55:02Z"));
        assertThat(factNight, is(Paths.get("2013", "12", "31")));

        //now test my birfday!!
        factNight = AuxCache.dateTimeStampToFACTPath(ZonedDateTime.parse("1987-09-20T20:55:02Z"));
        assertThat(factNight, is(Paths.get("1987", "09", "20")));
    }


    @Test
    public void testAuxFileFinder() throws Exception {
        URL u = AuxServiceTest.class.getResource("/dummy_files/aux/");
//        SourceURL url = new SourceURL(u);
        AuxFileService auxFileService = new AuxFileService();
        auxFileService.auxFolder = new SourceURL(u);

        ZonedDateTime night = ZonedDateTime.parse("2016-09-20T20:55:02Z");
        AuxCache.CacheKey key = new AuxCache().new CacheKey(AuxiliaryServiceName.FTM_CONTROL_STATE, night);
        Path path = Paths.get(auxFileService.auxFolder.getPath(), key.path.toString());
        //file should not exist
        assertFalse(path.toFile().exists());


        night = ZonedDateTime.parse("2014-09-20T20:55:02Z");
        key = new AuxCache().new CacheKey(AuxiliaryServiceName.FTM_CONTROL_STATE, night);
        path = Paths.get(auxFileService.auxFolder.getPath(), key.path.toString());

        assertTrue(path.toFile().exists());


        night = ZonedDateTime.parse("2014-09-20T20:55:02Z");
        key = new AuxCache().new CacheKey(AuxiliaryServiceName.RATE_SCAN_PROCESS_DATA, night);
        path = Paths.get(auxFileService.auxFolder.getPath(), key.path.toString());

        assertTrue(path.toFile().exists());


        night = ZonedDateTime.parse("2013-01-02T20:55:02Z");
        key = new AuxCache().new CacheKey(AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION, night);
        path = Paths.get(auxFileService.auxFolder.getPath(), key.path.toString());

        assertTrue(path.toFile().exists());


        night = ZonedDateTime.parse("2014-09-20T20:55:02Z");
        key = new AuxCache().new CacheKey(AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION, night);
        path = Paths.get(auxFileService.auxFolder.getPath(), key.path.toString());
        //file should not exist
        assertFalse(path.toFile().exists());
    }

    @Test
    public void testAuxFileService() throws Exception {
        URL u = AuxServiceTest.class.getResource("/dummy_files/aux/");
        AuxFileService s = new AuxFileService();
        s.auxFolder = new SourceURL(u);
        AuxPoint auxiliaryData = s.getAuxiliaryData(AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION, ZonedDateTime.parse("2013-01-02T23:30:21Z"), new Closest());
        assertThat(auxiliaryData, is(not(nullValue())));
        assertThat(auxiliaryData.getDouble("Ra"), is(not(nullValue())));
    }


    @Test
    public void testAuxPointGenericValue() throws Exception {
        URL u = AuxServiceTest.class.getResource("/dummy_files/aux/");
        AuxFileService s = new AuxFileService();
        s.auxFolder = new SourceURL(u);
        AuxPoint point = s.getAuxiliaryData(AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION, ZonedDateTime.parse("2013-01-02T21:30:21Z"), new Closest());
        Optional<Double> ra = point.getValue("Ra", Double.class);
        ra.orElseThrow(() -> new RuntimeException("Value is missing"));
    }

    /**
     * Test whether files in the specific aux folder are found
     */
    @Test
    public void testAuxFileServiceForSpecificLocation() throws Exception {
        URL u = AuxServiceTest.class.getResource("/dummy_files/aux/2013/01/02");
        AuxFileService s = new AuxFileService();
        s.auxFolder = new SourceURL(u);
        AuxPoint auxiliaryData = s.getAuxiliaryData(AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION, ZonedDateTime.parse("2013-01-02T23:30:21Z"), new Closest());
        assertThat(auxiliaryData, is(not(nullValue())));
        assertThat(auxiliaryData.getDouble("Ra"), is(not(nullValue())));
    }

    @Test
    public void testToGetAllPointsForNight() throws Exception {
        URL u = AuxServiceTest.class.getResource("/dummy_files/aux/");
        AuxFileService s = new AuxFileService();
        s.auxFolder = new SourceURL(u);
        SortedSet<AuxPoint> auxiliaryData = s.getAuxiliaryDataForWholeNight(
                AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION,
                ZonedDateTime.parse("2013-01-02T23:30:21Z")
        );

        assertFalse(auxiliaryData.isEmpty());
    }

}
