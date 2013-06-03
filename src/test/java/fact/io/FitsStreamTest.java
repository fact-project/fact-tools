/**
 * 
 */
package fact.io;

import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.io.SourceURL;

/**
 * @author kai
 * 
 */
public class FitsStreamTest {

	static Logger log = LoggerFactory.getLogger(FitsStreamTest.class);

	@Test
	public void testFitsStream() {

		try {
			URL u =  FitsStreamTest.class.getResource("/sample.fits.gz");
			SourceURL url = new SourceURL(u);
			FitsStream stream = new FitsStream(url);
			stream.init();

			Data item = stream.read();
			log.info( "size of data array: {}",
					((float[]) item.get("Data")).length 
					);
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
			URL u =  FitsStreamTest.class.getResource("/sample.fits.gz");
			SourceURL url = new SourceURL(u);
			FitsStream stream = new FitsStream(url);
			stream.init();

			Data item = stream.read();
			while (item != null) {
				if (!(item.containsKey("Data") && item.containsKey("EventNum") )){
					fail("fitsStream is not reading the right keys");
				}
				item = stream.read();
			}
			log.info("Read all the required keys");

		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not read FitsFile");
		}
	}
	
	@Test
	public void testDRSKeys(){
		try {
			URL u =  FitsStreamTest.class.getResource("/test.drs.fits.gz");
			SourceURL url = new SourceURL(u);
			FitsStream stream = new FitsStream(url);
			stream.init();

			Data item = stream.read();
			while (item != null) {
				if (!(item.containsKey("RunNumberBaseline")&& item.containsKey("GainMean") && item.containsKey("BaselineMean"))){
					fail("fitsStream is not reading the right keys");
				}
				item.get("TriggerOffsetMean");
				item.get("GainMean");
				item = stream.read();
			}
			log.info("Read all the required keys");
		} catch(ClassCastException e){
			fail("Wrong datatypes.");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not read FitsFile");
		}
	}
	
}
