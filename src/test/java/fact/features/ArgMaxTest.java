package fact.features;

import fact.photonstream.timeSeriesExtraction.ArgMax;
import junit.framework.Assert;
import org.junit.Test;

public class ArgMaxTest {

    @Test
    public void testArgMaxEmptyTimeSeries() {

        Throwable e = null;
        try {
            double[] emptyTimeSeries = {};
            ArgMax am = new ArgMax(emptyTimeSeries);
        } catch (Throwable ex) {
            e = ex;
        }
        Assert.assertTrue(e instanceof IndexOutOfBoundsException);
    }

    @Test
    public void testArgMaxZeroTimeSeries() {

        double[] zeroTimeSeries = {
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

        ArgMax am = new ArgMax(zeroTimeSeries);
        Assert.assertEquals(0, am.arg);
        Assert.assertEquals(0.0, am.max);
    }

    @Test
    public void testArgMaxSingleMaxTimeSeries() {

        double[] trianglePulse = {
                0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 4.0, 3.0, 2.0, 1.0, 0.0};

        ArgMax am = new ArgMax(trianglePulse);
        Assert.assertEquals(5, am.arg);
        Assert.assertEquals(5.0, am.max);
    }

    @Test
    public void testArgMaxTwoSameMaxima() {

        double[] samePulses = {
                0.0, 1.0, 2.0, 3.0, 2.0, 1.0, 0.0, 1.0, 2.0, 3.0, 2.0, 1.0, 0.0};

        // find the first of the two same maxima
        ArgMax am = new ArgMax(samePulses);
        Assert.assertEquals(3, am.arg);
        Assert.assertEquals(3.0, am.max);
    }

    @Test
    public void testArgMaxTwoFistLowSecondHigh() {

        double[] pulses = {
                0.0, 1.0, 2.0, 3.0, 2.0, 1.0, 0.0, 1.0, 2.0, 4.0, 2.0, 1.0, 0.0};
        //                 1st                           2nd
        ArgMax am = new ArgMax(pulses);
        Assert.assertEquals(9, am.arg);
        Assert.assertEquals(4.0, am.max);
    }

    @Test
    public void testArgMaxTwoFistHighSecondLow() {

        double[] pulses = {
                0.0, 1.0, 2.0, 4.0, 2.0, 1.0, 0.0, 1.0, 2.0, 3.0, 2.0, 1.0, 0.0};
        //                 1st                           2nd
        ArgMax am = new ArgMax(pulses);
        Assert.assertEquals(3, am.arg);
        Assert.assertEquals(4.0, am.max);
    }
}
