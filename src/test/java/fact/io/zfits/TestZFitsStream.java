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
	
	public void testDrsCalib() throws Exception {
		URL uZfits =  TestZFitsStream.class.getResource(zfits);
		URL ufits =  TestZFitsStream.class.getResource(fits);

		ZFitsStream stream = new ZFitsStream(new SourceURL(uZfits));
		stream.setTableName("Events");
		stream.init();
		
		ZFitsDrsCalib drsCalib = new ZFitsDrsCalib();
		drsCalib.setUrl(uZfits);
		
		TestFz testfz = new TestFz();
		testfz.setUrl(ufits);
		
		Data item = stream.readNext();
		while(item!=null) {
			testfz.process(item);
			item = stream.readNext();
		}
	}

	public void testReadFits() {
		
	}
	
	public void testZFitsVsFits() {
		
	}
}
