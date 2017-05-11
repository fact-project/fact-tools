package fact.rta;

import fact.auxservice.AuxPoint;
import fact.auxservice.AuxiliaryService;
import fact.io.hdureader.FITSStream;
import fact.rta.db.Run;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import stream.Data;
import stream.io.SourceURL;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Check some functions of the webserive. E.g. what happens when a new wild run suddenly appears in the tall grass.
 * Created by kai on 20.05.16.
 */
public class WebServiceTest {

    private URL dataUrl =  WebServiceTest.class.getResource("/testDataFile.fits.gz");
    private SourceURL url = new SourceURL(dataUrl);
    private FITSStream stream = new FITSStream(url);

    @Before
    public void setup() throws Exception {
        stream.init();
    }

    private Data prepareNextItem() throws Exception {
        Data item = stream.readNext();
        if (item == null){
            stream.init();
            item = stream.readNext();
        }
        item.put("Theta", 0.005);
        item.put("Theta_Off_1", 0.1);
        item.put("Theta_Off_2", 0.3);
        item.put("Theta_Off_3", 0.6*Math.random());
        item.put("Theta_Off_4", 0.7);
        item.put("Theta_Off_5", 0.1*Math.random());
        item.put("Size", 540.1);
        item.put("signal:prediction", 3*Math.random());
        item.put("signal:thetasquare", 0.05*Math.random());
        item.put("energy", 21356.7);
        item.put("SourceName", "Test Source");
        item.put("photoncharge", new Random().doubles(1440, 0.0, 5000.0).toArray());
        return item;
    }

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    public void startServer() throws Exception {
        RestWebService s = new RestWebService();
        File dbFile  = folder.newFile("data.sqlite");
        s.jdbcConnection = "jdbc:sqlite:"+dbFile.getCanonicalPath();


        Set<AuxPoint> ftmPoints = new HashSet<>();

        DateTime dateTime = DateTime.parse("2013-01-21T00:30:00+00:00");

        for (int run = 1; run < 100; run++) {
            DateTime startTime = dateTime.plusMinutes(5 * (run - 1));
            DateTime endTime = dateTime.plusMinutes(5 * run);
            for (int i = 0; i < 300; i++) {

                Data item = prepareNextItem();
                item.put("RUNID", run);
                item.put("DATE-OBS", startTime.toLocalDateTime().toString());
                item.put("DATE-END", endTime.toLocalDateTime().toString());

                DateTime utc = startTime.plusSeconds(i);
                Map<String, Serializable> map = new HashMap<>();
                map.put("OnTime", 4.0f);
                ftmPoints.add(new AuxPoint(utc, map));
//            ftmPoints.add(new AuxPoint(utc.plusMillis(500), map));

                s.updateEvent(OffsetDateTime.parse(utc.toString()), item, ftmPoints);
                OffsetDateTime now = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
                s.updateDataRate(now , new Random().nextDouble()*15 + 80);

                Thread.sleep(10);
            }
        }


        fail();
    }


    @Test
    public void testRunChange() throws Exception {
        //initialize db connection
        RestWebService s = new RestWebService();
        File dbFile  = folder.newFile("data.sqlite");
        s.jdbcConnection = "jdbc:sqlite:"+dbFile.getCanonicalPath();
        s.init();



        RTADataBase.DBInterface rtaTables = s.dbInterface;

        //create a few dummy items
        Data item = prepareNextItem();
        int night = (int) item.get("NIGHT");
        int runID = (int) item.get("RUNID");

        Set<AuxPoint> ftmPoints = new HashSet<>();


        for (int i = 0; i < 5; i++) {
            item = prepareNextItem();
            DateTime utc = AuxiliaryService.unixTimeUTCToDateTime(item).orElseThrow(Exception::new);
            Map<String, Serializable> map = new HashMap<>();
            map.put("OnTime", 4.0f);

            ftmPoints.add(new AuxPoint(utc, map));
            s.updateEvent(OffsetDateTime.parse(utc.toString()), item, ftmPoints);
        }


        Run factRun = rtaTables.getRun(night, runID);
        assertThat(factRun.onTime, is(Duration.ZERO));
        assertThat(factRun.health, is(RTADataBase.HEALTH.UNKNOWN));
        //create new run artificially to trigger updating of db entry
        item = prepareNextItem();
        item.put("NIGHT", night);
        item.put("RUNID", runID + 1);
        OffsetDateTime utc = AuxiliaryService.unixTimeUTCToDateTime(item).orElseThrow(Exception::new).toGregorianCalendar().toZonedDateTime().toOffsetDateTime();
        s.updateEvent(utc, item, ftmPoints);



        factRun = rtaTables.getRun(night, runID);
        assertThat(factRun.onTime, is(Duration.standardSeconds(20L)));
        assertThat(factRun.health, is(RTADataBase.HEALTH.OK));

        ftmPoints.clear();
        //now create a few more items for new run

        for (int i = 0; i < 5; i++) {
            item = prepareNextItem();
            item.put("NIGHT", night);
            item.put("RUNID", runID + 1);

            utc = AuxiliaryService.unixTimeUTCToDateTime(item).orElseThrow(Exception::new).toGregorianCalendar().toZonedDateTime().toOffsetDateTime();
            Map<String, Serializable> map = new HashMap<>();
            map.put("OnTime", 4.0f);
            ftmPoints.add(new AuxPoint(AuxiliaryService.unixTimeUTCToDateTime(item).orElseThrow(Exception::new), map));

            s.updateEvent(utc, item, ftmPoints);
        }

        factRun = rtaTables.getRun(night, runID + 1);
        assertThat(factRun.onTime, is(Duration.ZERO));
        assertThat(factRun.health, is(RTADataBase.HEALTH.IN_PROGRESS));
        //create new run artificially to trigger updating of db entry
        item = prepareNextItem();
        item.put("NIGHT", night);
        item.put("RUNID", runID + 2);
        utc = AuxiliaryService.unixTimeUTCToDateTime(item).orElseThrow(Exception::new).toGregorianCalendar().toZonedDateTime().toOffsetDateTime();
        s.updateEvent(utc, item, ftmPoints);


        factRun = rtaTables.getRun(night, runID + 1);
        assertThat(factRun.onTime, is(Duration.standardSeconds(20L)));
        assertThat(factRun.health, is(RTADataBase.HEALTH.OK));
    }
}
