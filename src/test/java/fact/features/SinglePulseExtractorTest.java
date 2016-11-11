package fact.features;

import fact.Utils;
import junit.framework.Assert;
import org.junit.Test;
import java.util.ArrayList;
import fact.features.singlePulse.timeLineExtraction.TemplatePulse;
import fact.features.singlePulse.timeLineExtraction.SinglePulseExtractor;
import fact.features.singlePulse.timeLineExtraction.Config;
import fact.features.singlePulse.timeLineExtraction.AddFirstArrayToSecondArray;

public class SinglePulseExtractorTest {

    @Test
    public void testEmptyTimeLineNoNoise() {

        double[] timeLine = new double[300];

        Config config = new Config();
        SinglePulseExtractor spe = new SinglePulseExtractor(config);
        int[] arrivalSlices = spe.getArrivalSlicesOnTimeline(timeLine);
        Assert.assertEquals(0, arrivalSlices.length);
    }

    @Test
    public void testOnePulseNoNoise() {

        Config config = new Config();
        SinglePulseExtractor spe = new SinglePulseExtractor(config);        

        for(int injectionSlice = 50; injectionSlice< 250; injectionSlice++) {

            double[] timeLine = new double[300];

            AddFirstArrayToSecondArray.at(
                TemplatePulse.factSinglePePulse(300),
                timeLine,
                injectionSlice);

            int[] arrivalSlices = spe.getArrivalSlicesOnTimeline(timeLine);

            Assert.assertEquals(1, arrivalSlices.length);

            Assert.assertTrue(
                arrivalSlices[0] <= injectionSlice+2 &&
                arrivalSlices[0] >= injectionSlice-2
            );
        }
    }

    @Test
    public void testSeveralPulsesOnTopOfEachOtherNoNoise() {

        Config config = new Config();
        config.maxIterations = 100;
        SinglePulseExtractor spe = new SinglePulseExtractor(config);  

        final int injectionSlice = 50;

        for(double amplitude = 0.0; amplitude<15.0; amplitude++) {
         
            double[] timeLine = new double[300];

            for(int i=0; i<(int)amplitude; i++)
                AddFirstArrayToSecondArray.at(
                    TemplatePulse.factSinglePePulse(300),
                    timeLine,
                    injectionSlice);

            int[]  arrivalSlices = spe.getArrivalSlicesOnTimeline(timeLine);

            Assert.assertTrue(
                (double)arrivalSlices.length <= amplitude+amplitude*0.25 &&
                (double)arrivalSlices.length >= amplitude-amplitude*0.25
            );
        }
    }

    @Test
    public void testSeveralPulsesInARowNoNoise() {

        Config config = new Config();
        config.maxIterations = 100;
        SinglePulseExtractor spe = new SinglePulseExtractor(config); 

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

        int[]  arrivalSlices = spe.getArrivalSlicesOnTimeline(timeLine);

        Assert.assertEquals(3, arrivalSlices.length);

        Assert.assertTrue((double)arrivalSlices[0] >= 50-2);
        Assert.assertTrue((double)arrivalSlices[0] <= 50+2);

        Assert.assertTrue((double)arrivalSlices[1] >= 125-2);
        Assert.assertTrue((double)arrivalSlices[1] <= 125+2);

        Assert.assertTrue((double)arrivalSlices[2] >= 200-2);
        Assert.assertTrue((double)arrivalSlices[2] <= 200+2);
    }
}
