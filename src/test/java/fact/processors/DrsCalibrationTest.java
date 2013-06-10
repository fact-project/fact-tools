package fact.processors;

import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.io.SourceURL;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;


public class DrsCalibrationTest {

	static Logger log = LoggerFactory.getLogger(DrsCalibrationTest.class);


	@Test
	public void drsCalibXML() {

		try {
			URL url = DrsCalibrationTest.class.getResource("/drsTest.xml");
			URL drsUrl =  FitsStreamTest.class.getResource("/test.drs.fits.gz");
			URL dataUrl =  FitsStreamTest.class.getResource("/sample.fits.gz");
			String[] args = {url.toString(), "-Dinput="+dataUrl.toString(), "-DdrsInput="+drsUrl.toString()};
			stream.run.main(args);
		} catch (Exception e) {
			fail("Could not run the ./drsTest.xml");
			e.printStackTrace();
		}
	}
	@Test
	public void drsCalibProcessor() {

		try {
			URL drsUrl =  FitsStreamTest.class.getResource("/test.drs.fits.gz");
			DrsCalibration pr = new DrsCalibration();
			pr.setUrl(drsUrl.toString());
			pr.setOutputKey("test");
			URL dataUrl =  FitsStreamTest.class.getResource("/sample.fits.gz");
			SourceURL url = new SourceURL(dataUrl);
			
			FitsStream stream = new FitsStream(url);
			stream.init();
			Data item = stream.read();
			while (item != null) {
				pr.process(item);
				if (!item.containsKey("test"))
					fail("Item does not contain the right key after drs calibration");
				//cant complete sanity checks because the test drs file does not fit the sample fits file.
//				float[] ar = (float[])item.get("test");
//				float[] data =  (float[])item.get("Data");
//				for (int i = 0; i < ar.length; ++i){
//					if (ar[i] <= data[i]){
//						fail("Calibrated data has to have a greater value");
//					}
//				}
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

