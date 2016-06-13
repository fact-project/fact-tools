package fact.rta;

import fact.io.FitsStream;
import fact.io.FitsStreamTest;
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
        item.put("energy", 123456.7);

        //TODO; get a aux service to do this
        item.put("onTime", 0.99);

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
        RTADataBase.FACTRun run = new RTADataBase().new FACTRun(item);
        rtaTables.insertRun(run);
        rtaTables.createSignalTable();

        DateTime eventTime = Signal.unixTimeUTCToDateTime((int[]) item.get("UnixTimeUTC")).orElseThrow(RuntimeException::new);
        RTADataBase.RTASignal s = new RTADataBase().new RTASignal(eventTime, item, run);
        rtaTables.insertSignal(s);
        rtaTables.insertSignal(s);

        List<String> signalEntries = rtaTables.getSignalEntries();
        assertThat(signalEntries.size(), is(1));

        item = prepareNextItem();

        eventTime = Signal.unixTimeUTCToDateTime((int[]) item.get("UnixTimeUTC")).orElseThrow(RuntimeException::new);
        s = new RTADataBase().new RTASignal(eventTime, item, run);
        rtaTables.insertSignal(s);
        rtaTables.insertSignal(s);

        signalEntries = rtaTables.getSignalEntries();
        assertThat(signalEntries.size(), is(2));
    }
}
