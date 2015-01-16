/**
 * 
 */
package fact.io;

import fact.filter.DrsCalibration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessorList;
import stream.io.SourceURL;
import stream.runtime.ProcessContextImpl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * @author chris
 * 
 */
public class FitsSpeedTest {

	static Logger log = LoggerFactory.getLogger(FitsSpeedTest.class);
	List<String> cols = new ArrayList<String>();

	@Test
	public void test() {

		int limit = 100;

		try {
			SourceURL url = new SourceURL(FitsStreamTest.class.getResource("/testDataFile.fits.gz"));
			FitsStream stream = new FitsStream(url);
			stream.init();

			ProcessorList preprocess = new ProcessorList();
			DrsCalibration drs = new DrsCalibration();
			URL u =  FitsStreamTest.class.getResource("/testDrsFile.drs.fits.gz");
			drs.setUrl(new SourceURL(u));
			preprocess.add(drs);
//			preprocess.add(new PhotonCharge());
//			preprocess.add(new MaxAmplitude());

			preprocess.init(new ProcessContextImpl());
			Long start = System.currentTimeMillis();
			Data item = stream.read();
			log.info( "size of data array: {}",
					((short[]) item.get("Data")).length 
					);
			int i = 0;
			while (item != null) {
				item = stream.read();
				item = preprocess.process(item);
				i++;
			}
			Long end = System.currentTimeMillis();
			Double seconds = (end - start) / 1000.0d;
			log.info("Reading {} rows took {} ms", i, end - start);
			log.info(" {} rows/sec", limit / seconds.doubleValue());

		} catch (Exception e) {
			e.printStackTrace();
			fail("Error: " + e.getMessage());
		}
	}

}