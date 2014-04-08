package fact.io.zfits;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.io.SourceURL;

public class TestZFitsStream {
	static Logger log = LoggerFactory.getLogger(TestZFitsStream.class);

	@Test
	public void test() throws Exception {
		URL u =  TestZFitsStream.class.getResource("/zfits_test.fz");
		ZFitsStream stream = new ZFitsStream(new SourceURL(u));
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
