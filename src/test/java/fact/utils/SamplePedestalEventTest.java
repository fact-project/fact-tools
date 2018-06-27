package fact.utils;

import org.junit.Before;
import org.junit.Test;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;

public class SamplePedestalEventTest {
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
