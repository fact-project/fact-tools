package fact.services;

import com.google.common.collect.HashBasedTable;
import fact.auxservice.AuxCache;
import fact.auxservice.AuxPoint;
import fact.auxservice.AuxiliaryServiceName;
import fact.auxservice.AuxFileService;
import fact.auxservice.strategies.AuxPointStrategy;
import fact.auxservice.strategies.Closest;
import org.joda.time.DateTime;
import org.junit.Test;
import stream.io.SourceURL;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
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
    public void testAuxFileFinder() throws Exception {
        URL u = AuxServiceTest.class.getResource("/dummy_files/aux/");
//        SourceURL url = new SourceURL(u);
        AuxFileService.AuxFileFinder auxFileFinder = new AuxFileService().new AuxFileFinder();
        Files.walkFileTree(new File(u.getFile()).toPath(), auxFileFinder);
        HashBasedTable<Integer, AuxiliaryServiceName, Path> table = auxFileFinder.auxFileTable;

        assertThat(table.size(), is(24));

        assertThat(table.contains(20140920, AuxiliaryServiceName.FTM_CONTROL_STATE), is(true));

        assertThat(table.contains(20130102, AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION), is(true));

        assertThat(table.contains(20140920, AuxiliaryServiceName.DRIVE_CONTROL_POINTING_POSITION), is(false));

        assertThat(table.contains(19870920, AuxiliaryServiceName.FTM_CONTROL_STATE), is(false));
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

}
