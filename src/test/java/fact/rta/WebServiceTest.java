package fact.rta;

import fact.auxservice.AuxiliaryService;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;
import fact.rta.db.Run;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.skife.jdbi.v2.DBI;
import stream.Data;
import stream.io.SourceURL;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.Random;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by kai on 20.05.16.
 */
public class WebServiceTest {

    URL dataUrl =  FitsStreamTest.class.getResource("/testDataFile.fits.gz");
    SourceURL url = new SourceURL(dataUrl);
    FitsStream stream = new FitsStream(url);

    @Before
    public void setup() throws Exception {
        stream.init();
    }

    Data prepareNextItem() throws Exception {
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

    @Test
    public void startServer() throws Exception {
        URL resource = JDBITest.class.getResource("/data.sqlite");
        RTAWebService s = new RTAWebService();
        s.sqlitePath = new SourceURL(resource);

        for (int i = 0; i < 3600; i++) {
            Thread.sleep(1100);

            DateTime now = DateTime.now();
            s.updateEvent(now, prepareNextItem());
            s.updateDataRate(now , new Random().nextDouble()*15 + 80);
        }
        fail();
    }


    @Test
    public void testRunChange() throws Exception {
        //initialize db connection
        RTAWebService s = new RTAWebService();
        File dbFile  = folder.newFile("data.sqlite");
        s.sqlitePath = new SourceURL("file:" + String.valueOf(dbFile));
        DBI dbi = new DBI("jdbc:sqlite:" + dbFile.getPath());
        RTADataBase.DBInterface rtaTables = dbi.open(RTADataBase.DBInterface.class);

        //create a few dummy items
        Data item = prepareNextItem();
        int night = (int) item.get("NIGHT");
        int runID = (int) item.get("RUNID");
        for (int i = 0; i < 5; i++) {
            item = prepareNextItem();
            Optional<DateTime> utc = AuxiliaryService.unixTimeUTCToDateTime(item);
            s.updateEvent(utc.orElseThrow(Exception::new), item);
        }


        Run factRun = rtaTables.getRun(night, runID);
        assertThat(factRun.onTime, is(0.0));
        assertThat(factRun.health, is(RTADataBase.HEALTH.UNKNOWN));
        //create new run artificially to trigger updating of db entry
        item = prepareNextItem();
        item.put("NIGHT", night);
        item.put("RUNID", runID + 1);
        Optional<DateTime> utc = AuxiliaryService.unixTimeUTCToDateTime(item);
        s.updateEvent(utc.orElseThrow(Exception::new), item);

        factRun = rtaTables.getRun(night, runID);
        assertThat(factRun.onTime, is(0.9));
        assertThat(factRun.health, is(RTADataBase.HEALTH.OK));
        //now create a few more items for new run
        for (int i = 0; i < 5; i++) {
            item = prepareNextItem();
            item.put("NIGHT", night);
            item.put("RUNID", runID + 1);
            utc = AuxiliaryService.unixTimeUTCToDateTime(item);
            s.updateEvent(utc.orElseThrow(Exception::new), item);
        }

        factRun = rtaTables.getRun(night, runID + 1);
        assertThat(factRun.onTime, is(0.0));
        assertThat(factRun.health, is(RTADataBase.HEALTH.UNKNOWN));
        //create new run artificially to trigger updating of db entry
        item = prepareNextItem();
        item.put("NIGHT", night);
        item.put("RUNID", runID + 2);
        utc = AuxiliaryService.unixTimeUTCToDateTime(item);
        s.updateEvent(utc.orElseThrow(Exception::new), item);


        factRun = rtaTables.getRun(night, runID + 1);
        assertThat(factRun.onTime, is(0.5));
        assertThat(factRun.health, is(RTADataBase.HEALTH.OK));

    }
}
