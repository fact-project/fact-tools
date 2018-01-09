/**
 *
 */
package fact.io;

import fact.io.hdureader.FITSStream;
import org.junit.Test;
import stream.Data;
import stream.io.SourceURL;

import java.net.URL;

import static org.junit.Assert.fail;

/**
 * @author kai
 */
public class FITSStreamTest {


    @Test
    public void testFitsStream() {

        try {
            URL u = FITSStreamTest.class.getResource("/testDataFile.fits.gz");
            SourceURL url = new SourceURL(u);
            FITSStream stream = new FITSStream(url);

            stream.init();
            Data item = stream.read();
            while (item != null) {
                item = stream.read();
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Could not read FitsFile");
        }
    }

    @Test
    public void testFitsKeys() {

        try {
            URL u = FITSStreamTest.class.getResource("/testDataFile.fits.gz");
            SourceURL url = new SourceURL(u);
            FITSStream stream = new FITSStream(url);
            stream.init();

            Data item = stream.read();
            while (item != null) {
                if (!(item.containsKey("Data") && item.containsKey("EventNum"))) {
                    fail("fitsStream is not reading the right keys");
                }
                item = stream.read();
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Could not read FitsFile");
        }
    }

}
