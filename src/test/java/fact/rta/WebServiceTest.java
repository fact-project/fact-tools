package fact.rta;

import fact.auxservice.AuxiliaryService;
import fact.io.hdureader.FITSStream;
import fact.rta.db.Run;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import stream.Data;
import stream.io.SourceURL;

import java.io.File;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Random;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Check some functions of the webserive. E.g. what happens when a new wild run suddenly appears in the tall grass.
 * Created by kai on 20.05.16.
 */
public class WebServiceTest {

    private URL dataUrl =  WebServiceTest.class.getResource("/testDataFile.fits.gz");
    private SourceURL url = new SourceURL(dataUrl);
    private FITSStream stream = new FITSStream(url);


    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();


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



    @Test
    public void testRunChange() throws Exception {

        File dbFile = temporaryFolder.newFile("test.sqlite");
        String jdbcConnection = "jdbc:sqlite:"+ dbFile.getCanonicalPath();
        SourceURL auxFolder = new SourceURL(WebServiceTest.class.getResource("/dummy_files/aux/"));
        WebSocketService service = WebSocketService.getService(jdbcConnection, auxFolder);

        //create a few dummy items
        Data item = prepareNextItem();
        int night = (int) item.get("NIGHT");
        int runID = (int) item.get("RUNID");



        for (int i = 0; i < 5; i++) {
            item = prepareNextItem();
            ZonedDateTime utc = AuxiliaryService.unixTimeUTCToDateTime(item).orElseThrow(Exception::new);

            service.updateEvent(ZonedDateTime.parse(utc.toString()), item);
        }


        Run factRun = service.dbInterface.withExtension(RTADataBase.class, dao -> dao.getRun(night, runID));
        assertThat(factRun.on_time_seconds, is(0L));
        assertThat(factRun.health, is(RTADataBase.HEALTH.UNKNOWN));

        //create new run artificially to trigger updating of db entry
        item = prepareNextItem();
        item.put("NIGHT", night);
        item.put("RUNID", runID + 1);
        ZonedDateTime utc = AuxiliaryService.unixTimeUTCToDateTime(item).orElseThrow(Exception::new);
        service.updateEvent(utc, item);



        factRun = service.dbInterface.withExtension(RTADataBase.class, dao -> dao.getRun(night, runID));
        assertThat(factRun.on_time_seconds, is(291L));
        assertThat(factRun.health, is(RTADataBase.HEALTH.OK));

        for (int i = 0; i < 5; i++) {
            item = prepareNextItem();
            item.put("NIGHT", night);
            item.put("RUNID", runID + 1);

            utc = AuxiliaryService.unixTimeUTCToDateTime(item).orElseThrow(Exception::new);
            service.updateEvent(utc, item);
        }

        factRun = service.dbInterface.withExtension(RTADataBase.class, dao -> dao.getRun(night, runID + 1));
        assertThat(factRun.on_time_seconds, is(0L));
        assertThat(factRun.health, is(RTADataBase.HEALTH.IN_PROGRESS));
        //create new run artificially to trigger updating of db entry
        item = prepareNextItem();
        item.put("NIGHT", night);
        item.put("RUNID", runID + 2);
        utc = AuxiliaryService.unixTimeUTCToDateTime(item).orElseThrow(Exception::new);
        service.updateEvent(utc, item);


        factRun = service.dbInterface.withExtension(RTADataBase.class, dao -> dao.getRun(night, runID + 1));
        assertThat(factRun.on_time_seconds, is(291L));
        assertThat(factRun.health, is(RTADataBase.HEALTH.OK));
    }
}
