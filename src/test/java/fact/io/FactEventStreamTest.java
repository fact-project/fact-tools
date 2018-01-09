/**
 *
 */
package fact.io;

import fact.io.hdureader.FITSStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.io.SourceURL;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * @author chris
 */
public class FactEventStreamTest {

    static Logger log = LoggerFactory.getLogger(FactEventStreamTest.class);

    final List<Data> events = new ArrayList<Data>();
    final ExpectedDataTypes typeCheck = new ExpectedDataTypes();

    @Before
    public void setup() throws Exception {
        typeCheck.addType("EventNum", new Integer(1));
        typeCheck.addType("TriggerNum", new Integer(1));
        typeCheck.addType("TriggerType", new Short((short) 4));
        typeCheck.addType("NumBoards", new Integer(40));
        typeCheck.addType("Errors", new byte[4]);

        events.clear();

        URL url = FactEventStreamTest.class.getResource("/fact-events.obj.gz");
        log.info("Reading serialized events from {}", url);

        ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(
                url.openStream()));

        Data item = (Data) ois.readObject();
        while (item != null) {
            item = removeSpecialKeys(item);
            //log.info("Adding event {}", item);
            events.add(item);
            try {
                item = (Data) ois.readObject();
            } catch (EOFException eof) {
                log.info("EndOfStream reached.");
                // eof.printStackTrace();
                break;
            }
        }

        //log.info("{} events read from {}.", events, url);
    }

    @Test
    public void testRead() throws Exception {

        URL u = FITSStreamTest.class.getResource("/testDataFile.fits.gz");
        SourceURL url = new SourceURL(u);
        log.info("Reading FITS events from {}", url);
        FITSStream fits = new FITSStream(url);

        fits.init();

        List<Data> evts = new ArrayList<Data>();
        Data item = fits.read();
        while (item != null && evts.size() < events.size()) {
            item = removeSpecialKeys(item);
            //log.info("Adding event {}", item);
            evts.add(item);
            item = fits.read();
        }

        fits.close();

        log.info("Read {} events from {}", evts.size(), url);
        Assert.assertEquals(events.size(), evts.size());

        for (int i = 0; i < 1 && i < events.size() && i < evts.size(); i++) {
            Data item2 = evts.get(i);
            Assert.assertTrue("Checking for equal keys", typeCheck.check(item2));
        }
    }

    private Data removeSpecialKeys(Data item) {
        Iterator<String> it = item.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (key.startsWith("@")) {
                // log.info("Removing key '{}'", key);
                it.remove();
            }
        }
        return item;
    }
}
