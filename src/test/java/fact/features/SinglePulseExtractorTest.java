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
    public void testEmptyTimeLineNoNoise() {

        double[] timeLine = new double[300];

        final int maxIterations = 50;
        ArrayList<Integer> arrivalSlices = SinglePulseExtractor.
            getArrivalSlicesOnTimeline(
                timeLine,
                maxIterations);

        Assert.assertEquals(0, arrivalSlices.size());
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
            ArrayList<Integer> arrivalSlices = SinglePulseExtractor.
                getArrivalSlicesOnTimeline(
                    timeLine,
                    maxIterations);

            Assert.assertEquals(1, arrivalSlices.size());

            Assert.assertTrue(
                arrivalSlices.get(0) <= injectionSlice+2 && 
                arrivalSlices.get(0) >= injectionSlice-2
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
            ArrayList<Integer> arrivalSlices = SinglePulseExtractor.
                getArrivalSlicesOnTimeline(
                    timeLine,
                    maxIterations);

            Assert.assertTrue(
                (double)arrivalSlices.size() <= amplitude+amplitude*0.15 &&
                (double)arrivalSlices.size() >= amplitude-amplitude*0.15
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
        ArrayList<Integer> arrivalSlices = SinglePulseExtractor.
            getArrivalSlicesOnTimeline(
                timeLine,
                maxIterations);

        Assert.assertEquals(3, arrivalSlices.size());

        Assert.assertTrue((double)arrivalSlices.get(0) >= 200-2);
        Assert.assertTrue((double)arrivalSlices.get(0) <= 200+2);

        Assert.assertTrue((double)arrivalSlices.get(1) >= 125-2);
        Assert.assertTrue((double)arrivalSlices.get(1) <= 125+2);

        Assert.assertTrue((double)arrivalSlices.get(2) >=  50-2);
        Assert.assertTrue((double)arrivalSlices.get(2) <=  50+2);
    }
}
