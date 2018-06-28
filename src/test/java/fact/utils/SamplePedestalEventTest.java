package fact.utils;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

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

    public class TestCase {
        public double[] result;
        public String[] input;

        public TestCase(double[] result, String[] input) {
            this.result = result;
            this.input = input;
        }
    }
    @Test
    public void testBinning() {
        ArrayList<TestCase> testCases = new ArrayList<>();
        testCases.add(new TestCase(new double[] {4.5,9.5,14.5,19.5,24.5,29.5,34.5,39.5,44.5,49.5,54.5,59.5,64.5,69.5,74.5,79.5,84.5,89.5}, new String[] {"5"}));
        testCases.add(new TestCase(new double[] {5.0,10.0,15.0,20.0,25.0,30.0,35.0}, new String [] {"5","10","15","20","25","30","35"}));

        for (TestCase test: testCases) {
            double[] testBinning = SamplePedestalEvent.createBinning(test.input);
            assertArrayEquals(test.result, testBinning, 0.01);
        }

    }
}
