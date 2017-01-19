package fact.features;

import fact.extraction.AboveThreshold;
import fact.Utils;
import stream.data.DataFactory;
import stream.Data;
import junit.framework.Assert;
import org.junit.Test;


public class AboveThresholdTest {

    @Test
    public void testAboveThresholdAllZero() {

        Data data = DataFactory.create();
        short[] raw_fact_data = new short[1440*300];

        data.put("data", raw_fact_data);
        data.put("NROI", 300);
        data.put("NPIX", 1440);

        AboveThreshold above = new AboveThreshold();
        above.setDataKey("data");
        above.setOutputKey("output");
        above.setThreshold((short)1);
        above.process(data);
        int[] pixelsAboveThreshold = (int[]) data.get("output");
        double ratio = (double) data.get("outputRatio");
        int count = (int) data.get("outputCount");
        Assert.assertEquals(0, count);
        Assert.assertEquals(0.0, ratio);
        Assert.assertEquals(0, pixelsAboveThreshold.length);
    }

    @Test
    public void testAboveThresholdCertainPixelsAbove() {

        Data data = DataFactory.create();
        short[] raw_fact_data = new short[1440*300];

        for(int pix=0; pix<1440; pix++) {
            for(int slice=0; slice<300; slice++) {
                final int pos = pix*300 + slice;
                if(pix==42 || pix==314 || pix==1337) {
                    raw_fact_data[pos] = 2000;
                }else{
                    raw_fact_data[pos] = 2;
                }
            }
        }

        data.put("data", raw_fact_data);
        data.put("NROI", 300);
        data.put("NPIX", 1440);

        AboveThreshold above = new AboveThreshold();
        above.setDataKey("data");
        above.setOutputKey("output");
        above.setThreshold((short)5);
        above.process(data);

        int[] pixelsAboveThreshold = (int[]) data.get("output");
        double ratio = (double) data.get("outputRatio");
        int count = (int) data.get("outputCount");
        Assert.assertEquals(3, count);
        Assert.assertEquals(3.0/1440.0, ratio);
        Assert.assertEquals(3, pixelsAboveThreshold.length);
        Assert.assertEquals(42, pixelsAboveThreshold[0]);
        Assert.assertEquals(314, pixelsAboveThreshold[1]);
        Assert.assertEquals(1337, pixelsAboveThreshold[2]);
    }

    @Test
    public void testAboveThresholdAllPixelsAbove() {

        Data data = DataFactory.create();
        short[] raw_fact_data = new short[1440*300];

        for(int i=0; i<1440*300; i++) {
            raw_fact_data[i] = 2000;
        }

        data.put("data", raw_fact_data);
        data.put("NROI", 300);
        data.put("NPIX", 1440);

        AboveThreshold above = new AboveThreshold();
        above.setDataKey("data");
        above.setOutputKey("output");
        above.setThreshold((short)5);
        above.process(data);

        int[] pixelsAboveThreshold = (int[]) data.get("output");
        double ratio = (double) data.get("outputRatio");
        int count = (int) data.get("outputCount");
        Assert.assertEquals(1440, count);
        Assert.assertEquals(1440.0/1440.0, ratio);
        Assert.assertEquals(1440, pixelsAboveThreshold.length);

        for(int i=0; i<1440; i++) {
            Assert.assertEquals(i, pixelsAboveThreshold[i]);
        }
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
        at.setDataKey("data");
        at.setOutputKey("output");
        at.setThreshold((short)10);
        at.process(data);

        int[] pixelsAboveThreshold = (int[]) data.get("output");
        double ratio = (double) data.get("outputRatio");
        int count = (int) data.get("outputCount");
        Assert.assertEquals(0, count);
        Assert.assertEquals(0.0, ratio);
        Assert.assertEquals(0, pixelsAboveThreshold.length);

        AboveThreshold at2 = new AboveThreshold();
        at2.setDataKey("data");
        at2.setOutputKey("output");
        at2.setThreshold((short)9);
        at2.process(data);

        pixelsAboveThreshold = (int[]) data.get("output");
        ratio = (double) data.get("outputRatio");
        count = (int) data.get("outputCount");
        Assert.assertEquals(1, count);
        Assert.assertEquals(1.0, ratio);
        Assert.assertEquals(1, pixelsAboveThreshold.length);
    }
}