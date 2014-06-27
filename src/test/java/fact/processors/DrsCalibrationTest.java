package fact.processors;

import fact.filter.DrsCalibration;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.io.SourceURL;

import java.net.URL;

import static org.junit.Assert.fail;


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
	/**
	 * starts a drs calibration and checks wether the resulting array is of the same array
	 * as the input array.
	 */
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
				try{
					double[] result = (double[]) item.get("test");
					short[] ar = (short[]) item.get("Data");
					if(ar.length != result.length){
						fail("drxCalibration is not working. the result array doesnt have the smae lenght as the original array");
					}
				} catch(ClassCastException e){
					fail("Failed to cast items to double[]");
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

