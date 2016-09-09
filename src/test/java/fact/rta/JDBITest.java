package fact.rta;

import fact.io.FitsStream;
import fact.io.FitsStreamTest;
import fact.rta.db.FACTRun;
import fact.rta.rest.RTASignal;
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
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Test some aspects of JDBI
 * Created by kai on 24.05.16.
 */
public class JDBITest {

    URL dataUrl =  FitsStreamTest.class.getResource("/testDataFile.fits.gz");
    SourceURL url = new SourceURL(dataUrl);
    FitsStream stream = new FitsStream(url);

    @Before
    public void setup() throws Exception {
        stream.init();
    }

    Data prepareNextItem() throws Exception {
        Data item = stream.read();
        item.put("Theta", 0.0);
        item.put("Theta_Off_1", 0.1);
        item.put("Theta_Off_2", 0.2);
        item.put("Theta_Off_3", 0.3);
        item.put("Theta_Off_4", 0.4);
        item.put("Theta_Off_5", 0.5);
        item.put("signal:prediction", 0.9);
        item.put("signal:thetasquare", 0.1);
        item.put("energy", 123456.7);
        return item;
    }

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Test
    public void testInsert() throws Exception {

        Data item = prepareNextItem();


        File dbFile  = folder.newFile("data.sqlite");
        DBI dbi = new DBI("jdbc:sqlite:" + dbFile.getPath());
        RTADataBase.DBInterface rtaTables = dbi.open(RTADataBase.DBInterface.class);

        rtaTables.createRunTable();
        FACTRun run = new FACTRun(item);
        rtaTables.insertRun(run);
        rtaTables.createSignalTable();

        DateTime eventTime = Signal.unixTimeUTCToDateTime((int[]) item.get("UnixTimeUTC")).orElseThrow(RuntimeException::new);
        RTASignal s = new RTASignal(eventTime, DateTime.now(), item, run);
        rtaTables.insertSignal(s);
        //second insert should be ignored
        rtaTables.insertSignal(s);

        List<RTASignal> signalEntries = rtaTables.getSignalEntries("2013-01-01", "2014-01-01");
        assertThat(signalEntries.size(), is(1));

        item = prepareNextItem();

        eventTime = Signal.unixTimeUTCToDateTime((int[]) item.get("UnixTimeUTC")).orElseThrow(RuntimeException::new);
        s = new RTASignal(eventTime, DateTime.now(),item, run);
        rtaTables.insertSignal(s);
        rtaTables.insertSignal(s);

        signalEntries = rtaTables.getSignalEntries("2013-01-01", "2014-01-01");
        assertThat(signalEntries.size(), is(2));
    }


    @Test
    public void testUpdateOnTime() throws Exception {

        Data item = prepareNextItem();


        File dbFile  = folder.newFile("data.sqlite");
        DBI dbi = new DBI("jdbc:sqlite:" + dbFile.getPath());
        RTADataBase.DBInterface rtaTables = dbi.open(RTADataBase.DBInterface.class);

        rtaTables.createRunTable();
        FACTRun run = new FACTRun(item);
        rtaTables.insertRun(run);

        FACTRun factRun = rtaTables.getRun(run.night, run.runID);
        assertThat(factRun.onTime, is(0.0));

        rtaTables.updateRunWithOnTime(0.99, run.runID, run.night);

        factRun = rtaTables.getRun(run.night, run.runID);
        assertThat(factRun.onTime, is(0.99));

        System.out.println(factRun);
    }



    @Test
    public void testUpdateHealth() throws Exception {

        Data item = prepareNextItem();


        File dbFile  = folder.newFile("data.sqlite");
        System.out.println(dbFile);

        DBI dbi = new DBI("jdbc:sqlite:" + dbFile.getPath());
        RTADataBase.DBInterface rtaTables = dbi.open(RTADataBase.DBInterface.class);
        rtaTables.createRunTable();

        FACTRun run = new FACTRun(item);
        rtaTables.insertRun(run);
        run = rtaTables.getRun(run.night, run.runID);
        assertThat(run.health, is(RTADataBase.HEALTH.UNKNOWN));

        rtaTables.updateRunHealth(RTADataBase.HEALTH.OK, run.runID, run.night);
        run = rtaTables.getRun(run.night, run.runID);
        assertThat(run.health, is(RTADataBase.HEALTH.OK));

        rtaTables.updateRunHealth(RTADataBase.HEALTH.BROKEN, run.runID, run.night);
        run = rtaTables.getRun(run.night, run.runID);
        assertThat(run.health, is(RTADataBase.HEALTH.BROKEN));
    }
}
