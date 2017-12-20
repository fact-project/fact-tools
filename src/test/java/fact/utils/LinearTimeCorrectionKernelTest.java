package fact.utils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LinearTimeCorrectionKernelTest {

    double[] t = {0.0, 1.0, 2.0, 3.0};
    double[] x = {2.0, 1.0, 0.0, -1.5};

    LinearTimeCorrectionKernel ltck = null;

    @Before
    public void setUp() throws Exception {
        ltck = new LinearTimeCorrectionKernel();
        ltck.fit(t, x);
    }

    @Test
    public void test() {

        double res = ltck.interpolate(-1);
        assertTrue("Left border test failed!", res == 2.0);

        res = ltck.interpolate(3.0);
        assertTrue("Rigth border test failed!", res == -1.5);

        res = ltck.interpolate(1.0);
        assertTrue("Equality test failed! " + res, res == 1.0);

        res = ltck.interpolate(1.5);
        assertTrue("Interpolation test failed! " + res, res == 0.5);

        res = ltck.interpolate(2.5);
        assertTrue("Interpolation test failed! " + res, res == -0.75);
    }

}
