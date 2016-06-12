package fact.rta;

import fact.io.FitsStream;
import fact.io.FitsStreamTest;
import org.joda.time.DateTime;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import stream.Data;
import stream.data.DataFactory;
import stream.io.SourceURL;

import java.net.URL;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Test some aspects of JDBI
 * Created by kai on 24.05.16.
 */
public class JDBITest {

    @Test
    public void testInsert() throws Exception {


        URL dataUrl =  FitsStreamTest.class.getResource("/testDataFile.fits.gz");
        SourceURL url = new SourceURL(dataUrl);
        FitsStream stream = new FitsStream(url);
        stream.init();
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

        String db = JDBITest.class.getResource("/data.sqlite").getPath();
        System.out.println(db);
        DBI dbi = new DBI("jdbc:sqlite:" + db);
        RTADataBase.DBInterface rtaTables = dbi.open(RTADataBase.DBInterface.class);
        rtaTables.createRunTable();


        RTADataBase.FACTRun run = new RTADataBase().new FACTRun(item);

        rtaTables.insertRun(run);

    }
}
