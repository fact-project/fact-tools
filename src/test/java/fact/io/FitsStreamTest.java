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

			// The following keys are required to exist in the DRS data
			final String[] drsKeys = new String[] { "RunNumberBaseline",
					"RunNumberGain", "RunNumberTriggerOffset", "BaselineMean",
					"BaselineRms", "GainMean", "GainRms", "TriggerOffsetMean",
			"TriggerOffsetRms" };

			Data item = stream.read();
			while (item != null) {
				for(String key : drsKeys){
					if (!(item.containsKey(key))){
						fail("fitsStream is not reading the right keys");
					}
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
	public void testDrsTypes(){
		try{
			URL u =  FitsStreamTest.class.getResource("/test.drs.fits.gz");
			SourceURL url = new SourceURL(u);
			FitsStream stream = new FitsStream(url);
			stream.init();

			String[] requiredFloatArrayKeys = {"BaselineMean","BaselineRms","TriggerOffsetMean","TriggerOffsetRms","GainMean","GainRms"};
			Data item = stream.read();
			@SuppressWarnings("unused")
			float[] ar;
			while (item != null) {
				for(String key : requiredFloatArrayKeys){
					if (!(item.containsKey(key))){
						fail("fitsStream is not reading the right keys");
					}
					ar = (float[]) item.get(key);
				}
				item = stream.read();
			}
			log.info("Read all the required keys");
			
		} catch(ClassCastException e){
			fail("Wrong datatzypes in the drs file");
			e.printStackTrace();
		} catch (Exception e){
			fail("Could not read FitsFile");
			e.printStackTrace();
		}
	}
}
