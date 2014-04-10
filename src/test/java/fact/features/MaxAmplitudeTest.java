package fact.features;

import fact.cleaning.CoreNeighborClean;
import fact.filter.DrsCalibration;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;
import fact.statistics.PixelDistribution2D;
import fact.utils.CutSlices;
import org.apache.commons.math3.util.Pair;
import org.junit.Before;
import org.junit.Test;
import stream.Data;
import stream.Processor;
import stream.io.SourceURL;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MaxAmplitudeTest {

    @Before
	private void setup() throws Exception{

		URL drsUrl =  FitsStreamTest.class.getResource("/test.drs.fits.gz");
		DrsCalibration pr = new DrsCalibration();
		pr.setUrl(drsUrl.toString());
		pr.setOutputKey("test");

		MaxAmplitudePosition pP = new MaxAmplitudePosition();
		pP.setKey("test");
		pP.setOutputKey("positions");

		MaxAmplitude maxAmp = new MaxAmplitude();
		maxAmp.setKey("test");
		maxAmp.setOutputKey("maxAmps");

		
	}
	
	@Test
	public void dataTypes() {

		try {
			URL dataUrl =  FitsStreamTest.class.getResource("/sample.fits.gz");
			SourceURL url = new SourceURL(dataUrl);

			FitsStream stream = new FitsStream(url);
			stream.init();
			Data item = stream.read();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not read stream");
		}
	}
}
