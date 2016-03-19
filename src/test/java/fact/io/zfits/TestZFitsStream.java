package fact.io.zfits;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.io.SourceURL;

import java.net.URL;

public class TestZFitsStream {
	static Logger log = LoggerFactory.getLogger(TestZFitsStream.class);


    /**
     * Test whether plain fits files can be read correctly
     * @throws Exception
     */
	@Test
	public void testReadFits() throws Exception {
		URL u =  TestZFitsStream.class.getResource("/testDataFile.fits.gz");
		ZFitsStream stream = new ZFitsStream(new SourceURL(u));
		stream.tableName = "Events";
		stream.init();
		
		log.info("Item number {}", 0);
		Data item = stream.read();
		log.info( "size of data array: {}",
				((short[]) item.get("Data")).length 
				);
		int i = 1;
        //the test file contains just 15 valid events.
		while (i < 15) {
			item = stream.read();
            log.debug("Item  {} has {} elements",i,  item.size());
            i++;
		}
	}
}
