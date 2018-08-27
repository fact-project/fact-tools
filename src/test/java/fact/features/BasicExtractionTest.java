package fact.features;

import fact.datacorrection.DrsCalibration;
import fact.extraction.BasicExtraction;
import fact.gainservice.GainService;
import fact.io.FITSStreamTest;
import fact.io.hdureader.FITSStream;
import org.junit.Before;
import org.junit.Test;
import stream.Data;
import stream.io.SourceURL;

import java.net.URL;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BasicExtractionTest {

    private DrsCalibration pr;
    private BasicExtraction extraction;

    private final String positionsKey = "maxPositions";
    private final String photonChargeKey = "photoncharge";

    @Before
    public void setup() throws Exception {
        URL drsUrl = FITSStreamTest.class.getResource("/testDrsFile.drs.fits.gz");
        pr = new DrsCalibration();
        pr.url = drsUrl.toString();
        pr.outputKey = "test";

        extraction = new BasicExtraction();
        extraction.dataKey  = "test";
        extraction.outputKeyMaxAmplPos  = positionsKey;
        extraction.outputKeyPhotonCharge = photonChargeKey;
        extraction.gainService = new GainService();
    }

    @Test
    public void dataTypes() {
        try {
            URL dataUrl = FITSStreamTest.class.getResource("/testDataFile.fits.gz");
            SourceURL url = new SourceURL(dataUrl);
            FITSStream stream = new FITSStream(url);
            stream.init();
            Data item = stream.read();
            pr.init(null);
            pr.process(item);
            extraction.process(item);
            assertTrue("Item did not contain the right key for maxAmplitude", item.containsKey(positionsKey) && item.containsKey(photonChargeKey));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Could not read stream");
        }
    }

    @Test
    public void maximum() {
        double[] mockNegatives = {-12.0, -11.1, -0.2, -0.2};
        int pos = extraction.calculateMaxPosition(0, 0, mockNegatives.length, mockNegatives.length, mockNegatives);
        assertTrue("Position should be array element 2, pos: " + pos, pos == 2);
        double[] mockPositives = {0.2, 0.3, 0.4, 120, 120, 120, 119.999};
        pos = extraction.calculateMaxPosition(0, 0, mockPositives.length, mockPositives.length, mockPositives);
        assertTrue("Position should be array element 3, pos: " + pos, pos == 3);
    }

    @Test
    public void extraction() {
        double[] data = {0.5, 2.0, 10.0, 12.0, 9.0, 5.0, 4.0};
        int maxPos = extraction.calculateMaxPosition(0, 0, data.length, data.length, data);
        assertTrue("Position should be array element 3, pos: " + maxPos, maxPos == 3);
        int halfHeightPos = extraction.calculatePositionHalfHeight(0, maxPos, 0, data.length, data);
        assertTrue("Half Height should be array element 2 , halfHeightPos: " + halfHeightPos, halfHeightPos == 2);
        double integral = extraction.calculateIntegral(0, halfHeightPos, 5, data.length, data);
        assertTrue("integral should have value 40 , integral: " + integral, integral == 40.0);
    }
}
