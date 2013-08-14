package fact.processors;

import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.io.SourceURL;
import fact.Constants;
import fact.features.MaxAmplitude;
import fact.features.MaxAmplitudePosition;
import fact.filter.DrsCalibration;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;


public class MaxAmplitudeTest {

	static Logger log = LoggerFactory.getLogger(MaxAmplitudeTest.class);


	@Test
	public void drsCalibProcessor() {

		try {
			
			URL drsUrl =  FitsStreamTest.class.getResource("/test.drs.fits.gz");
			DrsCalibration pr = new DrsCalibration();
			pr.setUrl(drsUrl.toString());
			pr.setOutputKey("test");
			
			MaxAmplitude maxAmp = new MaxAmplitude();
			maxAmp.setKey("test");
			maxAmp.setOutputKey("amps");
			

			MaxAmplitudePosition maxAmpPos = new MaxAmplitudePosition();
			maxAmpPos.setKey("test");
			maxAmpPos.setOutputKey("ampPos");
			
			URL dataUrl =  FitsStreamTest.class.getResource("/sample.fits.gz");
			SourceURL url = new SourceURL(dataUrl);
			
			FitsStream stream = new FitsStream(url);
			stream.init();
			Data item = stream.read();
			while (item != null) {
				pr.process(item);
				maxAmp.process(item);
				maxAmpPos.process(item);
				if (!item.containsKey("test"))
					fail("Item does not contain the right key after drs calibration");
				try{
					int[] pos = (int[]) item.get("ampPos");
					if(pos.length != Constants.NUMBEROFPIXEL){
						fail("MaxAmplitudePosition does not produce the right output");
					}
					float[] amps = (float[]) item.get("amps");
					if(amps.length != Constants.NUMBEROFPIXEL){
						fail("MaxAmplitude does not produce the right output");
					}
					
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

