package fact.extraction;

import fact.container.PixelSet;
import org.junit.Test;
import stream.Data;
import stream.data.DataFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class AboveThresholdTest {

    private double eps = 1e-12;


    @Test
    public void testAboveThresholdAllZero() {

        Data data = DataFactory.create();
        short[] raw_fact_data = new short[1440 * 300];

        data.put("data", raw_fact_data);
        data.put("NROI", 300);
        data.put("NPIX", 1440);

        AboveThreshold above = new AboveThreshold();
        above.dataKey = "data";
        above.outputKey = "output";
        above.threshold = 1;
        above.process(data);

        PixelSet pixelsAboveThreshold = (PixelSet) data.get("output");
        double pixelRatio = (double) data.get("outputPixelRatio");
        double sliceRatio = (double) data.get("outputSliceRatio");
        int pixelCount = (int) data.get("outputPixelCount");
        int sliceCount = (int) data.get("outputSliceCount");

        assertEquals(0, pixelCount);
        assertEquals(0, sliceCount);
        assertEquals(0.0, pixelRatio, eps);
        assertEquals(0.0, sliceRatio, eps);
        assertEquals(0, pixelsAboveThreshold.set.size());
    }

    @Test
    public void testAboveThresholdCertainPixelsAbove() {

        Data data = DataFactory.create();
        short[] raw_fact_data = new short[1440 * 300];

        PixelSet testPixels = new PixelSet();
        testPixels.addById(42);
        testPixels.addById(314);
        testPixels.addById(1337);

        for (int pix = 0; pix < 1440; pix++) {
            for (int slice = 0; slice < 300; slice++) {
                final int pos = pix * 300 + slice;
                if ((pix == 42 || pix == 314 || pix == 1337) && (slice == 271 || slice == 256)) {
                    raw_fact_data[pos] = 2000;
                } else {
                    raw_fact_data[pos] = 2;
                }
            }
        }

        data.put("data", raw_fact_data);
        data.put("NROI", 300);
        data.put("NPIX", 1440);

        AboveThreshold above = new AboveThreshold();
        above.dataKey = "data";
        above.outputKey = "output";
        above.threshold = 5;
        above.process(data);

        PixelSet pixelsAboveThreshold = (PixelSet) data.get("output");

        assertEquals(3, (int) data.get("outputPixelCount"));
        assertEquals(6, (int) data.get("outputSliceCount"));
        assertEquals(3.0 / 1440.0, data.get("outputPixelRatio"));
        assertEquals(6.0 / (1440.0 * 300.0), data.get("outputSliceRatio"));

        assertEquals(3, pixelsAboveThreshold.set.size());
        assertTrue(pixelsAboveThreshold.set.equals(testPixels.set));

    }

    @Test
    public void testAboveThresholdAllPixelsAbove() {

        Data data = DataFactory.create();
        short[] raw_fact_data = new short[1440 * 300];

        for (int i = 0; i < 1440 * 300; i++) {
            raw_fact_data[i] = 2000;
        }

        data.put("data", raw_fact_data);
        data.put("NROI", 300);
        data.put("NPIX", 1440);

        AboveThreshold above = new AboveThreshold();
        above.dataKey = "data";
        above.outputKey = "output";
        above.threshold = 5;
        above.process(data);

        PixelSet pixelsAboveThreshold = (PixelSet) data.get("output");
        assertEquals(1440, (int) data.get("outputPixelCount"));
        assertEquals(1440.0 / 1440.0, data.get("outputPixelRatio"));
        assertEquals(1440, pixelsAboveThreshold.set.size());

    }

    @Test
    public void testAboveThresholdJustBelow() {

        Data data = DataFactory.create();
        short[] raw_fact_data = new short[1];

        raw_fact_data[0] = 10;

        data.put("data", raw_fact_data);
        data.put("NROI", 1);
        data.put("NPIX", 1);

        AboveThreshold at = new AboveThreshold();
        at.dataKey  = "data";
        at.outputKey = "output";
        at.threshold = 10;
        at.process(data);

        PixelSet pixelsAboveThreshold = (PixelSet) data.get("output");

        assertEquals(0, (int) data.get("outputPixelCount"));
        assertEquals(0.0, data.get("outputPixelRatio"));
        assertEquals(0, pixelsAboveThreshold.set.size());

        AboveThreshold at2 = new AboveThreshold();
        at2.dataKey = "data";
        at2.outputKey = "output";
        at2.threshold = 9;
        at2.process(data);

        pixelsAboveThreshold = (PixelSet) data.get("output");
        assertEquals(1, (int) data.get("outputPixelCount"));
        assertEquals(1.0, data.get("outputPixelRatio"));
        assertEquals(1, pixelsAboveThreshold.set.size());
    }
}
