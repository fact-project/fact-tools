package fact.features;

import fact.Utils;
import junit.framework.Assert;
import org.junit.Test;
import fact.features.singlePulse.timeLineExtraction.ArgMax;

public class ArgMaxTest {

    @Test
    public void testArgMaxEmptyTimeLine() {

        Throwable e = null;
        try {
            double[] emptyTimeLine = {};
            ArgMax am = new ArgMax(emptyTimeLine);
        }catch(Throwable ex) {
            e = ex;
        }
        Assert.assertTrue(e instanceof IndexOutOfBoundsException);
    }

    @Test
    public void testArgMaxZeroTimeLine(){

        double[] zeroTimeLine = {
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

        ArgMax am = new ArgMax(zeroTimeLine);
        Assert.assertEquals(0, am.arg);
        Assert.assertEquals(0.0, am.max);
    }

    @Test
    public void testArgMaxSingleMaxTimeLine(){

        double[] trianglePulse = {
            0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 4.0, 3.0, 2.0, 1.0, 0.0};

        ArgMax am = new ArgMax(trianglePulse);
        Assert.assertEquals(5, am.arg);
        Assert.assertEquals(5.0, am.max);
    }

    @Test
    public void testArgMaxTwoSameMaxima(){

        double[] samePulses = {
            0.0, 1.0, 2.0, 3.0, 2.0, 1.0, 0.0, 1.0, 2.0, 3.0, 2.0, 1.0, 0.0};

        // find the first of the two same maxima
        ArgMax am = new ArgMax(samePulses);
        Assert.assertEquals(3, am.arg);
        Assert.assertEquals(3.0, am.max);
    }

    @Test
    public void testArgMaxTwoFistLowSecondHigh(){

        double[] pulses = {
            0.0, 1.0, 2.0, 3.0, 2.0, 1.0, 0.0, 1.0, 2.0, 4.0, 2.0, 1.0, 0.0};
        //                 1st                           2nd
        ArgMax am = new ArgMax(pulses);
        Assert.assertEquals(9, am.arg);
        Assert.assertEquals(4.0, am.max);
    }

    @Test
    public void testArgMaxTwoFistHighSecondLow(){

        double[] pulses = {
            0.0, 1.0, 2.0, 4.0, 2.0, 1.0, 0.0, 1.0, 2.0, 3.0, 2.0, 1.0, 0.0};
        //                 1st                           2nd
        ArgMax am = new ArgMax(pulses);
        Assert.assertEquals(3, am.arg);
        Assert.assertEquals(4.0, am.max);
    }
}
