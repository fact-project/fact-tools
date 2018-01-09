package fact.features;

import fact.photonstream.timeSeriesExtraction.AddFirstArrayToSecondArray;
import fact.photonstream.timeSeriesExtraction.SinglePulseExtractor;
import fact.photonstream.timeSeriesExtraction.TemplatePulse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SinglePulseExtractorTest {

    @Test
    public void testEmptytimeSeriesNoNoise() {

        double[] timeSeries = new double[300];

        SinglePulseExtractor.Config config = new SinglePulseExtractor.Config();
        SinglePulseExtractor spe = new SinglePulseExtractor(config);

        SinglePulseExtractor.Result result = spe.extractFromTimeSeries(timeSeries);
        assertEquals(0, result.pulseArrivalSlices.length);
        assertEquals(0, result.numberOfPulses());
    }


    @Test
    public void testFlatTimeSeriesBaseLine() {

        SinglePulseExtractor.Config config = new SinglePulseExtractor.Config();
        SinglePulseExtractor spe = new SinglePulseExtractor(config);

        for (double baseLine = -50.0; baseLine < 50; baseLine++) {

            double[] timeSeries = new double[300];
            for (int i = 0; i < timeSeries.length; i++)
                timeSeries[i] = baseLine;

            SinglePulseExtractor.Result result = spe.extractFromTimeSeries(timeSeries);

            assertEquals(0, result.numberOfPulses());
            assertTrue(
                    result.timeSeriesBaseLine() <= baseLine + 1.0 &&
                            result.timeSeriesBaseLine() >= baseLine - 1.0
            );
        }
    }

    @Test
    public void testOnePulseBaseLine() {

        SinglePulseExtractor.Config config = new SinglePulseExtractor.Config();
        SinglePulseExtractor spe = new SinglePulseExtractor(config);

        for (double baseLine = -50.0; baseLine < 50; baseLine++) {

            double[] timeSeries = new double[300];
            for (int i = 0; i < timeSeries.length; i++)
                timeSeries[i] = baseLine;

            AddFirstArrayToSecondArray.at(
                    TemplatePulse.factSinglePePulse(300),
                    timeSeries,
                    50);

            SinglePulseExtractor.Result result = spe.extractFromTimeSeries(timeSeries);

            assertEquals(1, result.numberOfPulses());
            assertTrue(
                    result.timeSeriesBaseLine() <= baseLine + 1.0 &&
                            result.timeSeriesBaseLine() >= baseLine - 1.0
            );
        }
    }

    @Test
    public void testOnePulseNoNoise() {

        SinglePulseExtractor.Config config = new SinglePulseExtractor.Config();
        SinglePulseExtractor spe = new SinglePulseExtractor(config);

        for (int injectionSlice = 50; injectionSlice < 250; injectionSlice++) {

            double[] timeSeries = new double[300];

            AddFirstArrayToSecondArray.at(
                    TemplatePulse.factSinglePePulse(300),
                    timeSeries,
                    injectionSlice);

            SinglePulseExtractor.Result result = spe.extractFromTimeSeries(timeSeries);

            assertEquals(1, result.pulseArrivalSlices.length);
            assertEquals(1, result.numberOfPulses());

            assertTrue(
                    result.pulseArrivalSlices[0] <= injectionSlice + 2 &&
                            result.pulseArrivalSlices[0] >= injectionSlice - 2
            );
        }
    }

    @Test
    public void testSeveralPulsesOnTopOfEachOtherNoNoise() {

        SinglePulseExtractor.Config config = new SinglePulseExtractor.Config();
        config.maxIterations = 100;
        SinglePulseExtractor spe = new SinglePulseExtractor(config);

        final int injectionSlice = 50;

        for (double amplitude = 0.0; amplitude < 15.0; amplitude++) {

            double[] timeSeries = new double[300];

            for (int i = 0; i < (int) amplitude; i++)
                AddFirstArrayToSecondArray.at(
                        TemplatePulse.factSinglePePulse(300),
                        timeSeries,
                        injectionSlice);

            SinglePulseExtractor.Result result = spe.extractFromTimeSeries(timeSeries);

            assertTrue(
                    (double) result.numberOfPulses() <= amplitude + amplitude * 0.25 &&
                            (double) result.numberOfPulses() >= amplitude - amplitude * 0.25
            );
        }
    }

    @Test
    public void testSeveralPulsesInARowNoNoise() {

        SinglePulseExtractor.Config config = new SinglePulseExtractor.Config();
        config.maxIterations = 100;
        SinglePulseExtractor spe = new SinglePulseExtractor(config);

        double[] timeSeries = new double[300];

        AddFirstArrayToSecondArray.at(
                TemplatePulse.factSinglePePulse(300),
                timeSeries,
                50);

        AddFirstArrayToSecondArray.at(
                TemplatePulse.factSinglePePulse(300),
                timeSeries,
                125);

        AddFirstArrayToSecondArray.at(
                TemplatePulse.factSinglePePulse(300),
                timeSeries,
                200);

        SinglePulseExtractor.Result result = spe.extractFromTimeSeries(timeSeries);


        assertEquals(3, result.pulseArrivalSlices.length);

        assertTrue((double) result.pulseArrivalSlices[0] >= 50 - 2);
        assertTrue((double) result.pulseArrivalSlices[0] <= 50 + 2);

        assertTrue((double) result.pulseArrivalSlices[1] >= 125 - 2);
        assertTrue((double) result.pulseArrivalSlices[1] <= 125 + 2);

        assertTrue((double) result.pulseArrivalSlices[2] >= 200 - 2);
        assertTrue((double) result.pulseArrivalSlices[2] <= 200 + 2);
    }

    @Test
    public void testResultPulseArrivalSlicesInRangeEmpty() {
        SinglePulseExtractor.Result result = new SinglePulseExtractor.Result();
        result.pulseArrivalSlices = new int[0];

        int[] myPulses = result.pulseArrivalSlicesInRange(0, 1000);
        assertEquals(0, myPulses.length);
    }

    @Test
    public void testResultPulseArrivalSlicesInRange() {
        SinglePulseExtractor.Result result = new SinglePulseExtractor.Result();
        result.pulseArrivalSlices = new int[]{10, 50, 100, 550, 1000};

        int[] myPulses = result.pulseArrivalSlicesInRange(50, 500);
        assertEquals(2, myPulses.length);
        assertEquals(50, myPulses[0]);
        assertEquals(100, myPulses[1]);
    }
}
