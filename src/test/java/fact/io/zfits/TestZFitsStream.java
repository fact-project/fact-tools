package fact.io.zfits;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.io.SourceURL;

import java.net.URL;

public class TestZFitsStream {
	static Logger log = LoggerFactory.getLogger(TestZFitsStream.class);

	private final String zfits = "/zfits_test.fz";
	private final String fits = "/fits_test.gz";


	//read all entries
	public void testReadZFits() throws Exception {
		URL u =  TestZFitsStream.class.getResource(zfits);
		ZFitsStream stream = new ZFitsStream(new SourceURL(u));
		stream.setTableName("Events");
		stream.init();
		
		log.info("Item number {}", 0);
		Data item = stream.read();
		log.info( "size of data array: {}",
				((short[]) item.get("Data")).length 
				);
		int i = 1;
		while (item != null) {
			log.info("Item number {}", i++);
			item = stream.read();
		}
	}
}
