/**
 *
 */
package fact.io;

import org.junit.Test;
import stream.Data;
import stream.io.SourceURL;
import fact.io.hdureader.FITSStream;
import java.net.URL;

import static org.junit.Assert.fail;

/**
 * @author kai
 *
 */
public class FITSStreamTest {


	@Test
	public void testFitsStream() {

		try {
			URL u =  FITSStreamTest.class.getResource("/testDataFile.fits.gz");
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
			URL u =  FITSStreamTest.class.getResource("/testDataFile.fits.gz");
			SourceURL url = new SourceURL(u);
			FITSStream stream = new FITSStream(url);
			stream.init();

			Data item = stream.read();
			while (item != null) {
				if (!(item.containsKey("Data") && item.containsKey("EventNum") )){
					fail("fitsStream is not reading the right keys");
				}
				item = stream.read();
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not read FitsFile");
		}
	}

	@Test
	public void testDRSKeys(){
		try {
			URL u =  FITSStreamTest.class.getResource("/testDrsFile.drs.fits.gz");
			SourceURL url = new SourceURL(u);
			FITSStream stream = new FITSStream(url);
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

		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not read FitsFile");
		}
	}

	@Test
	public void testDrsTypes(){
		try{
			URL u =  FITSStreamTest.class.getResource("/testDrsFile.drs.fits.gz");
			SourceURL url = new SourceURL(u);
			FITSStream stream = new FITSStream(url);
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

		} catch(ClassCastException e){
			fail("Wrong data types in the drs file");
			e.printStackTrace();
		} catch (Exception e){
			fail("Could not read FitsFile");
			e.printStackTrace();
		}
	}
	@Test
	public void testDriveFile(){
		try{
			URL u =  FITSStreamTest.class.getResource("/testDriveFile.fits");
			SourceURL url = new SourceURL(u);
			FITSStream stream = new FITSStream(url);
			stream.init();

			String[] requiredDoubleKeys = {"dev","dZd","dAz","Zd","Dec","Time", "Az"};
			Data item = stream.read();
			@SuppressWarnings("unused")
			double ar;
			while (item != null) {
				for(String key : requiredDoubleKeys){
					if (!(item.containsKey(key))){
						fail("fitsStream is not reading the right keys");
					}
					ar = (Double) item.get(key);
				}
				item = stream.read();
			}

		} catch(ClassCastException e){
			fail("Wrong data types in the drs file");
			e.printStackTrace();
		} catch (Exception e){
			fail("Could not read FitsFile");
			e.printStackTrace();
		}
	}
}
