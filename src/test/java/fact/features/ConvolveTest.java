package fact.features;

import fact.photonstream.timeSeriesExtraction.Convolve;
import junit.framework.Assert;
import org.junit.Test;

public class ConvolveTest {

    @Test
    public void testBoxWithDeltaSpike() {

        double[] deltaSpike = {1.0};

        double[] box = {
                0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0};

        double[] result = Convolve.firstWithSecond(box, deltaSpike);

        Assert.assertEquals(box.length, result.length);

        for (int i = 0; i < box.length; i++)
            Assert.assertEquals(box[i], result[i]);
    }

    @Test
    public void testBoxWithTriangle() {

        double[] triangle = {0.5, 1.0, 0.5};
        double[] box = {
                //       0    1    2    3    4    5    6    7
                0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                //  8    9   10   11   12   13    14   15
                1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0
        };

        double[] result = Convolve.firstWithSecond(box, triangle);

        Assert.assertEquals(18 - 3 + 1, result.length);
        Assert.assertEquals(0.0, result[0]);
        Assert.assertEquals(0.0, result[1]);
        Assert.assertEquals(0.5, result[2]);
        Assert.assertEquals(1.5, result[3]);
        Assert.assertEquals(2.0, result[4]);
        Assert.assertEquals(2.0, result[5]);
        Assert.assertEquals(2.0, result[6]);
        Assert.assertEquals(2.0, result[7]);
        Assert.assertEquals(2.0, result[8]);
        Assert.assertEquals(2.0, result[9]);
        Assert.assertEquals(2.0, result[10]);
        Assert.assertEquals(2.0, result[11]);
        Assert.assertEquals(1.5, result[12]);
        Assert.assertEquals(0.5, result[13]);
        Assert.assertEquals(0.0, result[14]);
        Assert.assertEquals(0.0, result[15]);
    }

    @Test
    public void testBoxWithZero() {

        double[] zeros = {0.0, 0.0, 0.0};
        double[] box = {
                //       0    1    2    3    4    5    6    7
                0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                //  8    9   10   11   12   13    14   15
                1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0
        };

        double[] result = Convolve.firstWithSecond(box, zeros);

        Assert.assertEquals(18 - 3 + 1, result.length);

        for (int i = 0; i < 16; i++)
            Assert.assertEquals(0.0, result[i]);
    }
}
