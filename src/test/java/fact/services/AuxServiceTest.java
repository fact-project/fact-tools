package fact.services;

import com.google.common.collect.HashBasedTable;
import fact.auxservice.AuxCache;
import fact.auxservice.AuxPoint;
import fact.auxservice.AuxiliaryServiceName;
import fact.auxservice.AuxFileService;
import fact.auxservice.strategies.Closest;
import org.joda.time.DateTime;
import org.junit.Test;
import stream.io.SourceURL;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedSet;


import static com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


/**
 * Created by kaibrugge on 17.11.14.
 */
public class AuxServiceTest {


    @Test
    public void testDateTimeToFACTNight(){
        Integer factNight = AuxCache.dateTimeStampToFACTNight(DateTime.parse("2014-01-02T23:55:02"));
        assertThat(factNight, is(20140102));

        //now test what happens after 00:00
        factNight = AuxCache.dateTimeStampToFACTNight(DateTime.parse("2014-01-03T00:55:02"));
        assertThat(factNight, is(20140102));

        //now test the next day
        factNight = AuxCache.dateTimeStampToFACTNight(DateTime.parse("2014-01-03T12:00:01"));
        assertThat(factNight, is(20140103));

        //now test what happens after newyears
        factNight = AuxCache.dateTimeStampToFACTNight(DateTime.parse("2014-01-01T00:55:02"));
        assertThat(factNight, is(20131231));

        //now test my birfday!!
        factNight = AuxCache.dateTimeStampToFACTNight(DateTime.parse("1987-09-20T20:55:02"));
        assertThat(factNight, is(19870920));
    }


    @Test
    public void testDateTimeToFACTPath(){
        Path factNight = AuxCache.dateTimeStampToFACTPath(DateTime.parse("2014-01-02T23:55:02"));

        assertThat(factNight, is(Paths.get("2014", "01", "02")));

        //now test what happens after 00:00
        factNight = AuxCache.dateTimeStampToFACTPath(DateTime.parse("2014-01-03T00:55:02"));
        assertThat(factNight, is(Paths.get("2014", "01", "02")));

        //now test the next day
        factNight = AuxCache.dateTimeStampToFACTPath(DateTime.parse("2014-01-03T12:00:01"));
        assertThat(factNight, is(Paths.get("2014", "01", "03")));

        //now test what happens after newyears
        factNight = AuxCache.dateTimeStampToFACTPath(DateTime.parse("2014-01-01T00:55:02"));
        assertThat(factNight, is(Paths.get("2013", "12", "31")));

        //now test my birfday!!
        factNight = AuxCache.dateTimeStampToFACTPath(DateTime.parse("1987-09-20T20:55:02"));
        assertThat(factNight, is(Paths.get("1987", "09", "20")));
    }


    @Test
    public void testAuxFileFinder() throws Exception {
        URL u = AuxServiceTest.class.getResource("/dummy_files/aux/");
//        SourceURL url = new SourceURL(u);
        AuxFileService auxFileService = new AuxFileService();
        auxFileService.auxFolder = new SourceURL(u);

        DateTime night = DateTime.parse("2016-09-20T20:55:02");
        AuxCache.CacheKey key = new AuxCache().new CacheKey(AuxiliaryServiceName.FTM_CONTROL_STATE, night);
        Path path = Paths.get(auxFileService.auxFolder.getPath(), key.path.toString());

        assertFalse(path.toFile().exists());



        night = DateTime.parse("2014-09-20T20:55:02");
        key = new AuxCache().new CacheKey(AuxiliaryServiceName.FTM_CONTROL_STATE, night);
        path = Paths.get(auxFileService.auxFolder.getPath(), key.path.toString());

        assertTrue(path.toFile().exists());


        night = DateTime.parse("2014-09-20T20:55:02");
        key = new AuxCache().new CacheKey(AuxiliaryServiceName.RATE_SCAN_PROCESS_DATA, night);
        path = Paths.get(auxFileService.auxFolder.getPath(), key.path.toString());

        assertTrue(path.toFile().exists());



        night = DateTime.parse("2013-01-02T20:55:02");
        key = new AuxCache().new CacheKey(AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION, night);
        path = Paths.get(auxFileService.auxFolder.getPath(), key.path.toString());

        assertTrue(path.toFile().exists());



        night = DateTime.parse("2014-09-20T20:55:02");
        key = new AuxCache().new CacheKey(AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION, night);
        path = Paths.get(auxFileService.auxFolder.getPath(), key.path.toString());

        assertFalse(path.toFile().exists());
    }

    @Test
    public void testAuxFileService() throws Exception {
        URL u = AuxServiceTest.class.getResource("/dummy_files/aux/");
//        SourceURL url = new SourceURL(u);
        AuxFileService s = new AuxFileService();
        s.auxFolder = new SourceURL(u);
        AuxPoint auxiliaryData = s.getAuxiliaryData(AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION, DateTime.parse("2013-01-02T23:30:21"), new Closest());
        assertThat(auxiliaryData, is(not(nullValue())));
        assertThat(auxiliaryData.getDouble("Ra"), is(not(nullValue())));
    }

    @Test
    public void testToGetAllPointsForNight() throws Exception {
        URL u = AuxServiceTest.class.getResource("/dummy_files/aux/");
//        SourceURL url = new SourceURL(u);
        AuxFileService s = new AuxFileService();
        s.auxFolder = new SourceURL(u);
        SortedSet<AuxPoint> auxiliaryData = s.getAuxiliaryDataForWholeNight(
                AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION,
                DateTime.parse("2013-01-02T23:30:21")
        );

        assertFalse(auxiliaryData.isEmpty());
    }

}
