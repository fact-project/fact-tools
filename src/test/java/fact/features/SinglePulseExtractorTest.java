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

        SinglePulseExtractor.Config config = new SinglePulseExtractor.Config();
        SinglePulseExtractor spe = new SinglePulseExtractor(config);
        
        SinglePulseExtractor.Result result = spe.extractFromTimeline(timeLine);
        Assert.assertEquals(0, result.pulseArrivalSlices.length);
        Assert.assertEquals(0, result.numberOfPulses());
    }


    @Test
    public void testFlatTimeLineBaseLine() {

        SinglePulseExtractor.Config config = new SinglePulseExtractor.Config();
        SinglePulseExtractor spe = new SinglePulseExtractor(config);  

        for(double baseLine=-50.0; baseLine<50; baseLine++) {

            double[] timeLine = new double[300];
            for(int i=0; i<timeLine.length; i++)
                timeLine[i] = baseLine;

            SinglePulseExtractor.Result result = spe.extractFromTimeline(timeLine);

            Assert.assertEquals(0, result.numberOfPulses());
            Assert.assertTrue(
                result.timeSeriesBaseLine() <= baseLine+1.0 &&
                result.timeSeriesBaseLine() >= baseLine-1.0
            );
        }
    }

    @Test
    public void testOnePulseBaseLine() {

        SinglePulseExtractor.Config config = new SinglePulseExtractor.Config();
        SinglePulseExtractor spe = new SinglePulseExtractor(config);        

        for(double baseLine=-50.0; baseLine<50; baseLine++) {

            double[] timeLine = new double[300];
            for(int i=0; i<timeLine.length; i++)
                timeLine[i] = baseLine;

            AddFirstArrayToSecondArray.at(
                TemplatePulse.factSinglePePulse(300),
                timeLine,
                50);

            SinglePulseExtractor.Result result = spe.extractFromTimeline(timeLine);

            Assert.assertEquals(1, result.numberOfPulses());
            Assert.assertTrue(
                result.timeSeriesBaseLine() <= baseLine+1.0 &&
                result.timeSeriesBaseLine() >= baseLine-1.0
            );
        }
    }

    @Test
    public void testOnePulseNoNoise() {

        SinglePulseExtractor.Config config = new SinglePulseExtractor.Config();
        SinglePulseExtractor spe = new SinglePulseExtractor(config);        

        for(int injectionSlice = 50; injectionSlice< 250; injectionSlice++) {

            double[] timeLine = new double[300];

            AddFirstArrayToSecondArray.at(
                TemplatePulse.factSinglePePulse(300),
                timeLine,
                injectionSlice);

            SinglePulseExtractor.Result result = spe.extractFromTimeline(timeLine);

            Assert.assertEquals(1, result.pulseArrivalSlices.length);
            Assert.assertEquals(1, result.numberOfPulses());

            Assert.assertTrue(
                result.pulseArrivalSlices[0] <= injectionSlice+2 &&
                result.pulseArrivalSlices[0] >= injectionSlice-2
            );
        }
    }

    @Test
    public void testSeveralPulsesOnTopOfEachOtherNoNoise() {

        SinglePulseExtractor.Config config = new SinglePulseExtractor.Config();
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

            SinglePulseExtractor.Result result = spe.extractFromTimeline(timeLine);

            Assert.assertTrue(
                (double)result.numberOfPulses() <= amplitude+amplitude*0.25 &&
                (double)result.numberOfPulses() >= amplitude-amplitude*0.25
            );
        }
    }

    @Test
    public void testSeveralPulsesInARowNoNoise() {

        SinglePulseExtractor.Config config = new SinglePulseExtractor.Config();
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

        SinglePulseExtractor.Result result = spe.extractFromTimeline(timeLine);


        Assert.assertEquals(3, result.pulseArrivalSlices.length);

        Assert.assertTrue((double)result.pulseArrivalSlices[0] >= 50-2);
        Assert.assertTrue((double)result.pulseArrivalSlices[0] <= 50+2);

        Assert.assertTrue((double)result.pulseArrivalSlices[1] >= 125-2);
        Assert.assertTrue((double)result.pulseArrivalSlices[1] <= 125+2);

        Assert.assertTrue((double)result.pulseArrivalSlices[2] >= 200-2);
        Assert.assertTrue((double)result.pulseArrivalSlices[2] <= 200+2);
    }
}
