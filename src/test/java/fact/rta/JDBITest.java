package fact.rta;


import fact.auxservice.AuxiliaryService;
import fact.io.hdureader.FITSStream;
import fact.rta.db.Run;
import fact.rta.db.Signal;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import stream.Data;
import stream.io.SourceURL;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


/**
 * Test some aspects of JDBI
 * Created by kai on 24.05.16.
 */
public class JDBITest {

    URL dataUrl =  JDBITest.class.getResource("/testDataFile.fits.gz");
    SourceURL url = new SourceURL(dataUrl);
    FITSStream stream = new FITSStream(url);

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
        item.put("SourceName", "Alien Planet");
        return item;
    }

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();


    @Test
    public void testParseDate(){
        String date  = "2013-01-02T21:46:35.427067";

        ZonedDateTime dt = LocalDateTime.parse(date).atZone(ZoneOffset.UTC);

        assertThat(dt.getHour(), is(21));
    }

    /**
     * test whether we can isnert a run into a database
     * @throws Exception
     */
    @Test
    public void testInsertRun() throws Exception {
        Data item = prepareNextItem();

        File dbFile  = folder.newFile("data.sqlite");
        Jdbi dbi = Jdbi.create("jdbc:sqlite:" + dbFile.getPath());
        dbi.installPlugin(new SqlObjectPlugin());

        Run run = dbi.withExtension(RTADataBase.class, dao -> {
            dao.createRunTableIfNotExists();
            Run r= new Run(item);
            dao.insertRun(r);
            return dao.getRun(r.night, r.run_id);
        });

        assertThat(run.run_id, is(60));
        assertThat(run.night, is(20130102));
        assertThat(run.health, is(RTADataBase.HEALTH.UNKNOWN));

    }

    /**
     * Test if we can add a run and then add signal entries to that run
     * @throws Exception
     */
    @Test
    public void testInsert() throws Exception {

        File dbFile  = folder.newFile("data.sqlite");
        Jdbi dbi = Jdbi.create("jdbc:sqlite:" + dbFile.getPath());
        dbi.installPlugin(new SqlObjectPlugin());

        List<fact.rta.db.Signal> signals = dbi.withExtension(RTADataBase.class, dao -> {
            Data item = prepareNextItem();
            dao.createRunTableIfNotExists();
            Run r = new Run(item);
            dao.insertRun(r);


            ZonedDateTime eventTime = AuxiliaryService.unixTimeUTCToDateTime(item).orElseThrow(RuntimeException::new);
            fact.rta.db.Signal s = new fact.rta.db.Signal(eventTime, ZonedDateTime.now(), item, r);


            dao.createSignalTableIfNotExists();

            dao.insertSignal(s);
            //second insert should be ignored
            dao.insertSignal(s);

            return dao.getAllSignalEntries();
        });

        assertThat(signals.size(), is(1));

        signals = dbi.withExtension(RTADataBase.class, dao -> {

            Data item = prepareNextItem();

            ZonedDateTime eventTime = AuxiliaryService.unixTimeUTCToDateTime(item).orElseThrow(RuntimeException::new);

            Run r = new Run(item);

            Signal s = new fact.rta.db.Signal(eventTime, ZonedDateTime.now(),item, r);
            dao.insertSignal(s);
            dao.insertSignal(s);

            return dao.getAllSignalEntries();
        });

        assertThat(signals.size(), is(2));
    }


    /**
     * Test if we can add a run and then add signal entries to that run
     * @throws Exception
     */
    @Test
    public void testDBTimestamps() throws Exception {

        Data item = prepareNextItem();

        File dbFile  = folder.newFile("data.sqlite");
        Jdbi dbi = Jdbi.create("jdbc:sqlite:" + dbFile.getPath());
        dbi.installPlugin(new SqlObjectPlugin());

        ZonedDateTime start = LocalDateTime.parse("2016-01-01T00:00:35").atZone(ZoneOffset.UTC);

        List<fact.rta.db.Signal> signalEntries = dbi.withExtension(RTADataBase.class, dao -> {
            dao.createRunTableIfNotExists();
            Run run = new Run(item);
            dao.insertRun(run);

            dao.createSignalTableIfNotExists();
            for (int i = 1; i < 11; i++) {
                ZonedDateTime eventTime = ZonedDateTime.parse(String.format("2016-01-%1$02dT00:33:22+00:00", i ));
                Signal s = new Signal(eventTime, ZonedDateTime.now(), item, run);
                dao.insertSignal(s);
            }


            return dao.getSignalEntriesBetweenDates(start, start.plusDays(11));
        });


        assertThat(signalEntries.size(), is(10));

        signalEntries = dbi.withExtension(RTADataBase.class, dao -> dao.getSignalEntriesBetweenDates(start, start.plusDays(5)));
        assertThat(signalEntries.size(), is(5));
    }


    @Test
    public void testBulkSignalInsert() throws Exception {
        Data item = prepareNextItem();

        File dbFile  = folder.newFile("data.sqlite");
        Jdbi dbi = Jdbi.create("jdbc:sqlite:" + dbFile.getPath());
        dbi.installPlugin(new SqlObjectPlugin());

        List<fact.rta.db.Signal> signalEntries = dbi.withExtension(RTADataBase.class, dao -> {
            dao.createRunTableIfNotExists();
            Run run = new Run(item);
            dao.insertRun(run);

            dao.createSignalTableIfNotExists();

            List<Signal> signals = IntStream.range(1, 21).mapToObj(i -> {
                ZonedDateTime eventTime = ZonedDateTime.parse(String.format("2016-01-%1$02dT00:33:22+00:00", i));
                return new Signal(eventTime, ZonedDateTime.now(), item, run);
            }).collect(toList());
            dao.insertSignals(signals.iterator());

            return dao.getAllSignalEntries();
        });
        assertThat(signalEntries.size(), is(20));
    }


    @Test
    public void testUpdateOnTime() throws Exception {
        Data item = prepareNextItem();

        File dbFile  = folder.newFile("data.sqlite");
        Jdbi dbi = Jdbi.create("jdbc:sqlite:" + dbFile.getPath());
        dbi.installPlugin(new SqlObjectPlugin());

        final Run factRun= dbi.withExtension(RTADataBase.class, dao -> {
                    dao.createRunTableIfNotExists();
                    Run run = new Run(item);
                    dao.insertRun(run);
                    return dao.getRun(run.night, run.run_id);
        });

        assertThat(factRun.on_time_seconds, is(0L));

        Run updatedRun = dbi.withExtension(RTADataBase.class, dao -> {
            dao.updateRunWithOnTime(factRun, 290);
            return dao.getRun(factRun.night, factRun.run_id);
        });

        assertThat(updatedRun.on_time_seconds, is(290L));
    }



    @Test
    public void testUpdateHealth() throws Exception {

        Data item = prepareNextItem();

        File dbFile  = folder.newFile("data.sqlite");
        Jdbi dbi = Jdbi.create("jdbc:sqlite:" + dbFile.getPath());
        dbi.installPlugin(new SqlObjectPlugin());

        final Run factRun= dbi.withExtension(RTADataBase.class, dao -> {
            dao.createRunTableIfNotExists();
            Run run = new Run(item);
            dao.insertRun(run);
            return dao.getRun(run.night, run.run_id);
        });


        assertThat(factRun.health, is(RTADataBase.HEALTH.UNKNOWN));

        Run updatedRun =  dbi.withExtension(RTADataBase.class, dao -> {
            dao.updateRunHealth(RTADataBase.HEALTH.OK, factRun);
            return dao.getRun(factRun.night, factRun.run_id);
        });

        assertThat(updatedRun.health, is(RTADataBase.HEALTH.OK));


        updatedRun =  dbi.withExtension(RTADataBase.class, dao -> {
            dao.updateRunHealth(RTADataBase.HEALTH.BROKEN, factRun);
            return dao.getRun(factRun.night, factRun.run_id);
        });

        assertThat(updatedRun.health, is(RTADataBase.HEALTH.BROKEN));

    }



    @Test
    public  void getAllRuns() throws Exception {

        File dbFile  = folder.newFile("data.sqlite");
        Jdbi dbi = Jdbi.create("jdbc:sqlite:" + dbFile.getPath());
        dbi.installPlugin(new SqlObjectPlugin());

        Set<Run> runs = dbi.withExtension(RTADataBase.class, dao -> {
            dao.createRunTableIfNotExists();

            for (int i = 0; i < 15; i++) {
                Data item = prepareNextItem();
                item.put("RUNID", i);
                Run run = new Run(item);
                dao.insertRun(run);
            }

            return dao.getAllRuns();
        });


        assertThat(runs.size(), is(15));

    }
}
