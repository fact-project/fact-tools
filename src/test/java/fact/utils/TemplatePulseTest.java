package fact.utils;

import fact.features.singlePulse.timeSeriesExtraction.TemplatePulse;
import junit.framework.Assert;
import org.junit.Test;

public class TemplatePulseTest {

    @Test
    public void testIfResultNear24() {

        double result = TemplatePulse.factSinglePePulseIntegral();
        System.out.println(result);
        Assert.assertTrue(
            Math.abs(result - 24.37) < 0.01
        );
    }

    @Test
    public void testIntegrateEmptySamples() {
        Throwable e = null;
        try {
            double[] samples = new double[]{};
            TemplatePulse.integrate(samples, 0, 10);
        }catch(Throwable ex) {
            e = ex;
        }
        Assert.assertTrue(e instanceof IndexOutOfBoundsException);
    }

    @Test
    public void testIntegrate() {
        double[] samples = new double[]{1,2,3};
        Assert.assertEquals(
            6.,
            TemplatePulse.integrate(samples, 0, 3)
        );
    }

    @Test
    public void testMean() {
        double[] foo = new double[]{1,2,3};
        Assert.assertEquals(
            2.,
            TemplatePulse.mean(foo)
        );
    }

    @Test
    public void testfindHalfHeightPosition() {
        double[] foo = new double[]{0.1, 0.3, 0.7, 1, 1, 0.5, 0.3};
        Assert.assertEquals(
            1,
            TemplatePulse.findHalfHeightPosition(foo, 1.)
        );
    }

}