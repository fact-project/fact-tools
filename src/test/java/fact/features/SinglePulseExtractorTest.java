package fact.features;

import fact.Utils;
import junit.framework.Assert;
import org.junit.Test;
import java.util.ArrayList;
import fact.features.singlePulse.timeLineExtraction.TemplatePulse;
import fact.features.singlePulse.timeLineExtraction.SinglePulseExtractor;
import fact.features.singlePulse.timeLineExtraction.AddFirstArrayToSecondArray;

public class SinglePulseExtractorTest {

    @Test
    public void testApplyAcCoupling() {

        double[] timeLine = {0, 5, 10, 0, 15, 0};
        double[] timeLine_expected = {-5, 0, 5, -5, 10, -5};

        SinglePulseExtractor.applyAcCoupling(timeLine);

        Assert.assertEquals(timeLine.length, timeLine_expected.length);

        for (int i = 0; i < timeLine_expected.length; i++) {
            Assert.assertEquals(timeLine[i], timeLine_expected[i]);
        }
    }

    @Test
    public void testApplyAcCouplingLength() {

        double[] timeLine = {0, 5, 10, 0, 15, 0};

        SinglePulseExtractor.applyAcCoupling(timeLine);

        Assert.assertEquals(timeLine.length, 6);
    }

    @Test
    public void testApplyAcCouplingEmpty() {

        double[] timeLine = new double[0];

        SinglePulseExtractor.applyAcCoupling(timeLine);

        Assert.assertEquals(timeLine.length, 0);
    }

    @Test
    public void testEmptyTimeLineNoNoise() {

        double[] timeLine = new double[300];

        final int maxIterations = 50;
        int[] arrivalSlices = SinglePulseExtractor.
            getArrivalSlicesOnTimeline(
                timeLine,
                maxIterations);

        Assert.assertEquals(0, arrivalSlices.length);
    }

    @Test
    public void testOnePulseNoNoise() {

        for(int injectionSlice = 50; injectionSlice< 250; injectionSlice++) {

            double[] timeLine = new double[300];

            AddFirstArrayToSecondArray.at(
                TemplatePulse.factSinglePePulse(300),
                timeLine,
                injectionSlice);

            SinglePulseExtractor.applyAcCoupling(timeLine);

            final int maxIterations = 50;
            int[] arrivalSlices = SinglePulseExtractor.
                getArrivalSlicesOnTimeline(
                    timeLine,
                    maxIterations);

            Assert.assertEquals(1, arrivalSlices.length);

            Assert.assertTrue(
                arrivalSlices[0] <= injectionSlice+2 &&
                arrivalSlices[0] >= injectionSlice-2
            );
        }
    }

    @Test
    public void testSeveralPulsesOnTopOfEachOtherNoNoise() {

        final int injectionSlice = 50;

        for(double amplitude = 0.0; amplitude<50.0; amplitude++) {
         
            double[] timeLine = new double[300];

            for(int i=0; i<(int)amplitude; i++)
                AddFirstArrayToSecondArray.at(
                    TemplatePulse.factSinglePePulse(300),
                    timeLine,
                    injectionSlice);

            SinglePulseExtractor.applyAcCoupling(timeLine);

            final int maxIterations = 100;
            int[]  arrivalSlices = SinglePulseExtractor.
                getArrivalSlicesOnTimeline(
                    timeLine,
                    maxIterations);

            Assert.assertTrue(
                (double)arrivalSlices.length <= amplitude+amplitude*0.15 &&
                (double)arrivalSlices.length >= amplitude-amplitude*0.15
            );
        }
    }

    @Test
    public void testSeveralPulsesInARowNoNoise() {

        double[] timeLine = new double[300];

        AddFirstArrayToSecondArray.at(
            TemplatePulse.factSinglePePulse(300),
            timeLine,
            50);

        AddFirstArrayToSecondArray.at(
            TemplatePulse.factSinglePePulse(300),
            timeLine,
            125);

        AddFirstArrayToSecondArray.at(
            TemplatePulse.factSinglePePulse(300),
            timeLine,
            200);

        SinglePulseExtractor.applyAcCoupling(timeLine);

        final int maxIterations = 100;
        int[]  arrivalSlices = SinglePulseExtractor.
            getArrivalSlicesOnTimeline(
                timeLine,
                maxIterations);

        Assert.assertEquals(3, arrivalSlices.length);

        Assert.assertTrue((double)arrivalSlices[0] >= 200-2);
        Assert.assertTrue((double)arrivalSlices[0] <= 200+2);

        Assert.assertTrue((double)arrivalSlices[1] >= 125-2);
        Assert.assertTrue((double)arrivalSlices[1] <= 125+2);

        Assert.assertTrue((double)arrivalSlices[2] >=  50-2);
        Assert.assertTrue((double)arrivalSlices[2] <=  50+2);
    }
}
