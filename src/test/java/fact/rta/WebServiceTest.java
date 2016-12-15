package fact.rta;

import fact.auxservice.AuxPoint;
import fact.auxservice.AuxiliaryService;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;
import fact.rta.db.Run;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.skife.jdbi.v2.DBI;
import stream.Data;
import stream.io.SourceURL;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Check some functions of the webserive. E.g. what happens when a new wild run suddenly appears in the tall grass.
 * Created by kai on 20.05.16.
 */
public class WebServiceTest {

    private URL dataUrl =  FitsStreamTest.class.getResource("/testDataFile.fits.gz");
    private SourceURL url = new SourceURL(dataUrl);
    private FitsStream stream = new FitsStream(url);

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
        item.put("Theta_Off_3", 0.6);
        item.put("Theta_Off_4", 0.7);
        item.put("Theta_Off_5", 0.1);
        item.put("Size", 540.1);
        item.put("signal:prediction", 0.9);
        item.put("signal:thetasquare", 0.1);
        item.put("energy", 21356.7);
        item.put("SourceName", "Test Source");
        item.put("photoncharge", new Random().doubles(1440, 0.0, 5000.0).toArray());
        return item;
    }

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

//    @Test
//    public void startServer() throws Exception {
//        RTAWebService s = new RTAWebService();
//        s.jdbcConnection = "jdbc:sqlite:./test.sqlite";
//
//        for (int i = 0; i < 3600; i++) {
//            Thread.sleep(1100);
//
//            DateTime now = DateTime.now();
//            s.updateEvent(now, prepareNextItem());
//            s.updateDataRate(now , new Random().nextDouble()*15 + 80);
//        }
//        fail();
//    }


    @Test
    public void testRunChange() throws Exception {
        //initialize db connection
        RTAWebService s = new RTAWebService();
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
            s.updateEvent(utc, item, ftmPoints);
        }


        Run factRun = rtaTables.getRun(night, runID);
        assertThat(factRun.onTime, is(Duration.ZERO));
        assertThat(factRun.health, is(RTADataBase.HEALTH.UNKNOWN));
        //create new run artificially to trigger updating of db entry
        item = prepareNextItem();
        item.put("NIGHT", night);
        item.put("RUNID", runID + 1);
        DateTime utc = AuxiliaryService.unixTimeUTCToDateTime(item).orElseThrow(Exception::new);
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

            utc = AuxiliaryService.unixTimeUTCToDateTime(item).orElseThrow(Exception::new);
            Map<String, Serializable> map = new HashMap<>();
            map.put("OnTime", 4.0f);
            ftmPoints.add(new AuxPoint(utc, map));

            s.updateEvent(utc, item, ftmPoints);
        }

        factRun = rtaTables.getRun(night, runID + 1);
        assertThat(factRun.onTime, is(Duration.ZERO));
        assertThat(factRun.health, is(RTADataBase.HEALTH.IN_PROGRESS));
        //create new run artificially to trigger updating of db entry
        item = prepareNextItem();
        item.put("NIGHT", night);
        item.put("RUNID", runID + 2);
        utc = AuxiliaryService.unixTimeUTCToDateTime(item).orElseThrow(Exception::new);
        s.updateEvent(utc, item, ftmPoints);


        factRun = rtaTables.getRun(night, runID + 1);
        assertThat(factRun.onTime, is(Duration.standardSeconds(20L)));
        assertThat(factRun.health, is(RTADataBase.HEALTH.OK));
    }
}
