package fact.features;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.datacorrection.DrsCalibration;
import fact.extraction.MaxAmplitude;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;
import stream.Data;
import stream.data.DataFactory;
import stream.io.SourceURL;
import stream.runtime.setup.ParameterInjection;
import stream.util.Variables;

public class MaxAmplitudeTest {

    static Logger log = LoggerFactory.getLogger(MaxAmplitudeTest.class);
    private MaxAmplitude maxAmp;
    private DrsCalibration pr;

    private final String outputKey = "maxAmps";

    @Before
    public void setup() throws Exception {

        URL drsUrl = FitsStreamTest.class.getResource("/testDrsFile.drs.fits.gz");
        pr = new DrsCalibration();
        pr.setUrl(new SourceURL(drsUrl));
        pr.setOutputKey("test");

        maxAmp = new MaxAmplitude();
        maxAmp.setKey("test");
        maxAmp.setOutputKey(outputKey);

    }

    @Test
    public void dataTypes() {

        try {
            URL dataUrl = FitsStreamTest.class.getResource("/testDataFile.fits.gz");
            SourceURL url = new SourceURL(dataUrl);
            FitsStream stream = new FitsStream(url);
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
        double[] mock = { 0.0, 0.1, 0.2, 0.3, -0.3, -0.4, -0.5, 128.0, -128.0, 4123.0, 4123.00001, 4123.00001, 1230,
                12 };
        double[] maxValues = new double[mock.length];
        int[] maxPos = new int[mock.length];
        double max = maxAmp.maximum(mock.length, 0, mock, 0, mock.length, maxValues, maxPos);
        assertTrue("Maximum should be 4123.00001", max == 4123.00001);
    }

    @Test
    public void testMaxWithInterval() {
        String key = "mock";
        int npix = 16;
        int nroi = 32;

        int trueMaxPos = 10;

        double[] mock = new double[npix * nroi];
        for (int pixel = 0; pixel < npix; pixel++) {
            for (int slice = 0; slice < nroi; slice++) {
                int idx = pixel * nroi + slice;
                mock[idx] = 25.0;
                if (slice == trueMaxPos) {
                    mock[idx] = 50.0;
                }

                // slice values outside the window will even be higher thatn the
                // max inside the window
                //
                if (slice < 5 || slice > 25) {
                    mock[idx] = 100.0;
                }

                log.info("pixel {}, slice {} = " + mock[idx], pixel, slice);
            }
        }

        Data item = DataFactory.create();
        item.put("NPIX", npix);
        item.put(key, mock);

        try {
            Map<String, String> params = new LinkedHashMap<String, String>();
            params.put("window", "5,20");
            params.put("key", "mock");
            params.put("outputKey", "amplitude:max");
            ParameterInjection.inject(maxAmp, params, new Variables());

            item = maxAmp.process(item);
            double[] maxValues = (double[]) item.get("amplitude:max");
            int[] maxPos = (int[]) item.get("amplitude:max:pos");

            for (int i = 0; i < npix; i++) {
                log.info("maximum of pixel {} is at {}, value is: " + maxValues[i], i, maxPos[i]);
                Assert.assertTrue(maxPos[i] == trueMaxPos);
                Assert.assertTrue(maxValues[i] == 50.0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
