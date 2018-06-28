package fact.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class SamplePedestalEventTest {

    @Test
    public void testSetBinningEdges() {
        String[] binning = {"1","2","3"};
        double[] expected = new double[]{1.,2.,3.};
        SamplePedestalEvent samplePedestalEvent = new SamplePedestalEvent();
        samplePedestalEvent.setBinning(binning);
        assertArrayEquals(expected, samplePedestalEvent.bins, 0.1   );
    }

    @Test
    public void testSetBinningWidth() {
        String[] binning = {"2"};

        double[] expected = new double[45];
        for (int i = 0; i < 45; i++) {
            expected[i] = i*2.+1.5;
        }
        SamplePedestalEvent samplePedestalEvent = new SamplePedestalEvent();
        samplePedestalEvent.setBinning(binning);
        assertArrayEquals(expected, samplePedestalEvent.bins, 0.1   );
    }

    @Test
    public void testGetBin() {
        String[] binning = {"2"};
        double value[] = {1,4,6};
        int expectedBin[] = {0,2,3};
        SamplePedestalEvent samplePedestalEvent = new SamplePedestalEvent();
        samplePedestalEvent.setBinning(binning);
        for (int i = 0; i < value.length; i++) {
            int bin = samplePedestalEvent.getBin(value[i]);
            assertEquals(bin, expectedBin[i]);
        }

    }
}