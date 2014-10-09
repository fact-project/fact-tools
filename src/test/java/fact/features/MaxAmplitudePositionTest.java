package fact.features;

import fact.extraction.MaxAmplitudePosition;
import fact.filter.DrsCalibration;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;
import org.junit.Before;
import org.junit.Test;
import stream.Data;
import stream.io.SourceURL;

import java.net.URL;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MaxAmplitudePositionTest {

    private MaxAmplitudePosition maxAmpPos;
    private DrsCalibration pr;

    private final String outputKey = "maxAmps";

    @Before
	public void setup() throws Exception{

		URL drsUrl =  FitsStreamTest.class.getResource("/testDrsFile.drs.fits.gz");
        pr = new DrsCalibration();
		pr.setUrl(drsUrl.toString());
		pr.setOutputKey("test");

        maxAmpPos = new MaxAmplitudePosition();
		maxAmpPos.setKey("test");
		maxAmpPos.setOutputKey(outputKey);
	}
	
	@Test
	public void dataTypes() {

		try {
			URL dataUrl =  FitsStreamTest.class.getResource("/testDataFile.fits.gz");
			SourceURL url = new SourceURL(dataUrl);
			FitsStream stream = new FitsStream(url);
			stream.init();
			Data item = stream.read();
            pr.process(item);
            maxAmpPos.process(item);
            assertTrue("Item did not contain the right key for maxAmplitude", item.containsKey(outputKey));

		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not read stream");
		}
	}

    @Test
    public void maximum() {
        double[] mockNegatives = {-12.0, -11.1, -0.2, -0.2};
        maxAmpPos.setSearchWindowRight(mockNegatives.length);
        maxAmpPos.setSearchWindowLeft(0);
        double pos = maxAmpPos.findMaximumPosition(0, mockNegatives.length, mockNegatives);
        assertTrue("Position should be array element 2 ", pos == 2 );

        double[] mockPositives = {0.2, 0.3, 0.4, 120, 120, 120, 119.999};
        pos = maxAmpPos.findMaximumPosition(0, mockPositives.length, mockPositives);
        assertTrue("Position should be array element 3 ", pos == 3 );

    }
}
