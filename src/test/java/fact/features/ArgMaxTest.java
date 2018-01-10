package fact.features;

import fact.photonstream.timeSeriesExtraction.ArgMax;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        assertTrue(e instanceof IndexOutOfBoundsException);
    }

    @Test
    public void testArgMaxZeroTimeSeries() {

        double[] zeroTimeSeries = {
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

        ArgMax am = new ArgMax(zeroTimeSeries);
        assertEquals(0, am.arg);
        assertEquals(0.0, am.max, 1e-12);
    }

    @Test
    public void testArgMaxSingleMaxTimeSeries() {

        double[] trianglePulse = {
                0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 4.0, 3.0, 2.0, 1.0, 0.0};

        ArgMax am = new ArgMax(trianglePulse);
        assertEquals(5, am.arg);
        assertEquals(5.0, am.max, 1e-12);
    }

    @Test
    public void testArgMaxTwoSameMaxima() {

        double[] samePulses = {
                0.0, 1.0, 2.0, 3.0, 2.0, 1.0, 0.0, 1.0, 2.0, 3.0, 2.0, 1.0, 0.0};

        // find the first of the two same maxima
        ArgMax am = new ArgMax(samePulses);
        assertEquals(3, am.arg);
        assertEquals(3.0, am.max, 1e-12);
    }

    @Test
    public void testArgMaxTwoFistLowSecondHigh() {

        double[] pulses = {
                0.0, 1.0, 2.0, 3.0, 2.0, 1.0, 0.0, 1.0, 2.0, 4.0, 2.0, 1.0, 0.0};
        //                 1st                           2nd
        ArgMax am = new ArgMax(pulses);
        assertEquals(9, am.arg);
        assertEquals(4.0, am.max, 1e-12);
    }

    @Test
    public void testArgMaxTwoFistHighSecondLow() {

        double[] pulses = {
                0.0, 1.0, 2.0, 4.0, 2.0, 1.0, 0.0, 1.0, 2.0, 3.0, 2.0, 1.0, 0.0};
        //                 1st                           2nd
        ArgMax am = new ArgMax(pulses);
        assertEquals(3, am.arg);
        assertEquals(4.0, am.max, 1e-12);
    }
}
