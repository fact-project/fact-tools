package fact.features;

import fact.datacorrection.DrsCalibration;
import fact.extraction.MaxAmplitude;
import fact.io.FITSStreamTest;
import fact.io.hdureader.FITSStream;
import org.junit.Before;
import org.junit.Test;
import stream.Data;
import stream.io.SourceURL;

import java.net.URL;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MaxAmplitudeTest {

    private MaxAmplitude maxAmp;
    private DrsCalibration pr;

    private final String outputKey = "maxAmps";

    @Before
    public void setup() throws Exception {

        URL drsUrl = FITSStreamTest.class.getResource("/testDrsFile.drs.fits.gz");
        pr = new DrsCalibration();
        pr.url = drsUrl.toString();
        pr.outputKey = "test";


        maxAmp = new MaxAmplitude();
        maxAmp.key = "test";
        maxAmp.outputKey = outputKey;


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
            maxAmp.process(item);
            assertTrue("Item did not contain the right key for maxAmplitude", item.containsKey(outputKey));

        } catch (Exception e) {
            e.printStackTrace();
            fail("Could not read stream");
        }
    }

    @Test
    public void maximum() {
        double[] mock = {0.0, 0.1, 0.2, 0.3, -0.3, -0.4, -0.5, 128.0, -128.0, 4123.0, 4123.00001, 4123.00001, 1230, 12};
        double max = maxAmp.maximum(mock.length, 0, mock);
        assertTrue("Maximum should be 4123.00001", max == 4123.00001);
    }
}
