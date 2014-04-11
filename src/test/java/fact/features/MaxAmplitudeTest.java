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

    private MaxAmplitude maxAmp;
    private DrsCalibration pr;

    private final String outputKey = "maxAmps";

    @Before
	public void setup() throws Exception{

		URL drsUrl =  FitsStreamTest.class.getResource("/test.drs.fits.gz");
        pr = new DrsCalibration();
		pr.setUrl(drsUrl.toString());
		pr.setOutputKey("test");


        maxAmp = new MaxAmplitude();
		maxAmp.setKey("test");
		maxAmp.setOutputKey(outputKey);

		
	}
	
	@Test
	public void dataTypes() {

		try {
			URL dataUrl =  FitsStreamTest.class.getResource("/sample.fits.gz");
			SourceURL url = new SourceURL(dataUrl);
			FitsStream stream = new FitsStream(url);
			stream.init();
			Data item = stream.read();
            pr.process(item);
            maxAmp.process(item);
            assertTrue("Item did not contain the right key for maxAmplitude", item.containsKey(outputKey));

		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not read stream");
		}
	}

    @Test
    public void maximum() {
        double[] mock = {0.0, 0.1, 0.2, 0.3, -0.3, -0.4 ,-0.5 , 128.0, -128.0, 4123.0, 4123.00001, 4123.00001, 1230,12};
        double max = maxAmp.maximum(mock.length, 0, mock);
        assertTrue("Maximum should be 4123.00001", max == 4123.00001);
    }
}
