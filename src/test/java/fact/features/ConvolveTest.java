package fact.features;

import fact.photonstream.timeSeriesExtraction.Convolve;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConvolveTest {

    @Test
    public void testBoxWithDeltaSpike() {

        double[] deltaSpike = {1.0};

        double[] box = {
                0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0,
                1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0};

        double[] result = Convolve.firstWithSecond(box, deltaSpike);

        assertEquals(box.length, result.length);

        for (int i = 0; i < box.length; i++)
            assertEquals(box[i], result[i], 1e-12);
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

        assertEquals(18 - 3 + 1, result.length, 1e-12);
        assertEquals(0.0, result[0], 1e-12);
        assertEquals(0.0, result[1], 1e-12);
        assertEquals(0.5, result[2], 1e-12);
        assertEquals(1.5, result[3], 1e-12);
        assertEquals(2.0, result[4], 1e-12);
        assertEquals(2.0, result[5], 1e-12);
        assertEquals(2.0, result[6], 1e-12);
        assertEquals(2.0, result[7], 1e-12);
        assertEquals(2.0, result[8], 1e-12);
        assertEquals(2.0, result[9], 1e-12);
        assertEquals(2.0, result[10], 1e-12);
        assertEquals(2.0, result[11], 1e-12);
        assertEquals(1.5, result[12], 1e-12);
        assertEquals(0.5, result[13], 1e-12);
        assertEquals(0.0, result[14], 1e-12);
        assertEquals(0.0, result[15], 1e-12);
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

        assertEquals(18 - 3 + 1, result.length);

        for (int i = 0; i < 16; i++)
            assertEquals(0.0, result[i], 1e-12);
    }
}
