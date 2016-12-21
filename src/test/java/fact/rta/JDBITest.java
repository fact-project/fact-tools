package fact.rta;


import fact.auxservice.AuxiliaryService;

import fact.io.FitsStream;
import fact.io.FitsStreamTest;
import fact.rta.db.Run;
import fact.rta.db.Signal;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Seconds;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.skife.jdbi.v2.DBI;
import stream.Data;
import stream.io.SourceURL;
import sun.awt.image.OffScreenImage;

import java.io.File;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


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

    private Data prepareNextItem() throws Exception {
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

    /**
     * test whether we can isnert a run into a database
     * @throws Exception
     */
    @Test
    public void testInsertRun() throws Exception {
        Data item = prepareNextItem();

        File dbFile  = folder.newFile("data.sqlite");
        DBI dbi = new DBI("jdbc:sqlite:" + dbFile.getPath());
        RTADataBase.DBInterface rtaTables = dbi.open(RTADataBase.DBInterface.class);

        rtaTables.createRunTableIfNotExists();
        Run run = new Run(item);
        rtaTables.insertRun(run);
    }

    /**
     * Test if we can add a run and then add signal entries to that run
     * @throws Exception
     */
    @Test
    public void testInsert() throws Exception {

        Data item = prepareNextItem();


        File dbFile  = folder.newFile("data.sqlite");
        DBI dbi = new DBI("jdbc:sqlite:" + dbFile.getPath());
        RTADataBase.DBInterface rtaTables = dbi.open(RTADataBase.DBInterface.class);

        rtaTables.createRunTableIfNotExists();
        Run run = new Run(item);
        rtaTables.insertRun(run);

        rtaTables.createSignalTableIfNotExists();


        OffsetDateTime eventTime = AuxiliaryService.unixTimeUTCToOffsetDateTime(item).orElseThrow(RuntimeException::new);
        Signal s = new Signal(eventTime, OffsetDateTime.now(), item, run);
        rtaTables.insertSignal(s);
        //second insert should be ignored
        rtaTables.insertSignal(s);

        List<Signal> signalEntries = rtaTables.getAllSignalEntries();
        assertThat(signalEntries.size(), is(1));

        item = prepareNextItem();


        eventTime = AuxiliaryService.unixTimeUTCToOffsetDateTime(item).orElseThrow(RuntimeException::new);

        s = new Signal(eventTime, OffsetDateTime.now(),item, run);
        rtaTables.insertSignal(s);
        rtaTables.insertSignal(s);

        signalEntries = rtaTables.getAllSignalEntries();
        assertThat(signalEntries.size(), is(2));
    }

    /**
     * Test if we can add a run and then add signal entries to that run
     * @throws Exception
     */
    @Test
    public void testDBTimestamps() throws Exception {

        Data item = prepareNextItem();


        File dbFile  = folder.newFile("data.sqlite");
        DBI dbi = new DBI("jdbc:sqlite:" + dbFile.getPath());
        RTADataBase.DBInterface rtaTables = dbi.open(RTADataBase.DBInterface.class);

        rtaTables.createRunTableIfNotExists();
        Run run = new Run(item);
        rtaTables.insertRun(run);

        rtaTables.createSignalTableIfNotExists();
        for (int i = 1; i < 11; i++) {
            OffsetDateTime eventTime = OffsetDateTime.parse(String.format("2016-01-%1$02dT00:33:22+00:00", i ));
            Signal s = new Signal(eventTime, OffsetDateTime.now(), item, run);
            rtaTables.insertSignal(s);
        }

        List<Signal> signalEntries = rtaTables.getSignalEntriesBetweenDates("2016-01-01", "2016-01-30");
        assertThat(signalEntries.size(), is(10));

        signalEntries = rtaTables.getSignalEntriesBetweenDates("2016-01-01", "2016-01-06");
        assertThat(signalEntries.size(), is(5));

        rtaTables.createSignalTableIfNotExists();
        for (int i = 1; i < 60; i++) {
            OffsetDateTime eventTime = OffsetDateTime.parse(String.format("2016-02-01T00:%1$02d:22+00:00", i ));
            Signal s = new Signal(eventTime, OffsetDateTime.now(), item, run);
            rtaTables.insertSignal(s);
        }

        signalEntries = rtaTables.getSignalEntriesBetweenDates("2016-01-01", "2016-01-06");
        assertThat(signalEntries.size(), is(5));
    }


    @Test
    public void testUpdateOnTime() throws Exception {

        Data item = prepareNextItem();


        File dbFile  = folder.newFile("data.sqlite");
        DBI dbi = new DBI("jdbc:sqlite:" + dbFile.getPath());
        RTADataBase.DBInterface rtaTables = dbi.open(RTADataBase.DBInterface.class);

        rtaTables.createRunTableIfNotExists();
        Run run = new Run(item);
        rtaTables.insertRun(run);

        Run factRun = rtaTables.getRun(run.night, run.runID);
        assertThat(factRun.onTime, is(Duration.ZERO));
        rtaTables.updateRunWithOnTime(run, 290.0);

        factRun = rtaTables.getRun(run.night, run.runID);
        assertThat(factRun.onTime.toStandardSeconds(), is(Seconds.seconds(290)));
    }



    @Test
    public void testUpdateHealth() throws Exception {

        Data item = prepareNextItem();


        File dbFile  = folder.newFile("data.sqlite");
        System.out.println(dbFile);

        DBI dbi = new DBI("jdbc:sqlite:" + dbFile.getPath());
        RTADataBase.DBInterface rtaTables = dbi.open(RTADataBase.DBInterface.class);
        rtaTables.createRunTableIfNotExists();

        Run run = new Run(item);
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


    @Test
    public  void getAllRuns() throws Exception {
        File dbFile  = folder.newFile("data.sqlite");
        DBI dbi = new DBI("jdbc:sqlite:" + dbFile.getPath());
        RTADataBase.DBInterface rtaTables = dbi.open(RTADataBase.DBInterface.class);

        rtaTables.createRunTableIfNotExists();

        for (int i = 0; i < 15; i++) {
            Data item = prepareNextItem();
            item.put("RUNID", i);
            Run run = new Run(item);
            rtaTables.insertRun(run);
        }

        Set<Run> allRuns = rtaTables.getAllRuns();
        assertThat(allRuns.size(), is(15));

    }
}
