/**
 *
 */
package fact.io;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Keys;
import stream.data.DataFactory;

import java.io.File;
import java.net.URL;


/**
 * @author maxnoe
 */
public class FITSWriterTest {
    static Logger log = LoggerFactory.getLogger(FITSWriterTest.class);

	@Test
	public void testFitsWriterNoItems() throws Exception {
        FITSWriter fitsWriter = new FITSWriter();
        File f = File.createTempFile("test_fits", ".fits");
        log.info(f.getAbsolutePath());

        URL url = new URL("file:" + f.getAbsolutePath());
        fitsWriter.setUrl(url);
        fitsWriter.init(null);
        fitsWriter.finish();
    }

    @Test
    public void testFitsWriter() throws Exception {
        FITSWriter fitsWriter = new FITSWriter();
        File f = File.createTempFile("test_fits", ".fits");
        log.info(f.getAbsolutePath());

        URL url = new URL("file:" + f.getAbsolutePath());
        fitsWriter.setUrl(url);
        fitsWriter.setKeys(new Keys("*"));

        fitsWriter.init(null);

        Data item = DataFactory.create();
        item.put("EventNum", 1);
        item.put("TriggerType", 4);
        item.put("NROI", 300);
        item.put("NPIX", 1440);
        item.put("x", 0.0);
        item.put("y", 5.0);
        item.put("array", new double[]{1.0, 2.0, 3.0});

        fitsWriter.process(item);
        Assert.assertEquals(fitsWriter.getNumEventsWritten(), 1);
        fitsWriter.finish();
    }
}
