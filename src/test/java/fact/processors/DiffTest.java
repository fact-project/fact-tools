package fact.processors;

import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.io.SourceURL;
import fact.filter.DrsCalibration;
import fact.filter.MovingAverage;
import fact.io.FitsStream;
import fact.utils.Diff;


public class DiffTest {

	static Logger log = LoggerFactory.getLogger(DiffTest.class);


	@Test
	public void drsCalibProcessor() {

		try {
			
			URL drsUrl =  DiffTest.class.getResource("/test.drs.fits.gz");
			DrsCalibration pr = new DrsCalibration();
			pr.setUrl(drsUrl.toString());
			pr.setOutputKey("test");
			
			MovingAverage a = new MovingAverage();
			a.setKey("test");
			a.setOutputKey("out");
			
			Diff d= new Diff();
			d.setKeyA("test");
			d.setKeyB("out");
			d.setOutputKey("out");
			
			URL dataUrl =  DiffTest.class.getResource("/sample.fits.gz");
			SourceURL url = new SourceURL(dataUrl);
			
			FitsStream stream = new FitsStream(url);
			stream.init();
			Data item = stream.read();
			while (item != null) {
				pr.process(item);
				a.process(item);
				d.process(item);
				if (!item.containsKey("test"))
					fail("Item does not contain the right key after drs calibration");
				if (!item.containsKey("out"))
					fail("Item does not contain the right key after diff operator");
				
				try{
					@SuppressWarnings("unused")
					float[] amps = (float[]) item.get("out");
					amps = (float[]) item.get("test");
					
				} catch(ClassCastException e){
					fail("Failed to cast items to float[]");
				}
				item = stream.read();
			}
			
		} catch(ClassCastException e){
			fail("Wrong datatypes.");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not execute drsCalibration");
		}
	}
}

