package fact.features;

import fact.utils.ElementWise;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class ElementWiseTest {

    @Test
    public void testMultiplyEmpty() {

        double[] empty = {};

        double[] result = ElementWise.multiply(empty, 1.0);
        assertEquals(0, empty.length);
        assertEquals(0, result.length);
    }

    @Test
    public void testMultiplyOnes() {

        double[] ones = {1.0, 1.0, 1.0, 1.0, 1.0};

        double[] result = ElementWise.multiply(ones, 1.0);
        assertEquals(ones.length, result.length);

        for (int i = 0; i < ones.length; i++)
            assertEquals(1.0, result[i]);
    }

    @Test
    public void testMultiplyArrayOnesSeveralScalars() {

        double[] ones = {1.0, 1.0, 1.0, 1.0, 1.0};

        for (int i = -13; i < 37; i++) {
            double scalar = (double) (i);
            double[] result = ElementWise.multiply(ones, scalar);
            assertEquals(ones.length, result.length);

            for (int j = 0; j < ones.length; j++)
                assertEquals(scalar, result[j]);
        }
    }

    @Test
    public void testAddEmpty() {

        double[] empty = {};

        double[] result = ElementWise.add(empty, 1.0);
        assertEquals(0, empty.length);
        assertEquals(0, result.length);
    }

    @Test
    public void testAddZeros() {

        double[] zeros = {0.0, 0.0, 0.0, 0.0, 0.0};

        double[] result = ElementWise.add(zeros, 1.0);
        assertEquals(5, zeros.length);
        assertEquals(5, result.length);

        for (int i = 0; i < zeros.length; i++)
            assertEquals(1.0, result[i]);
    }

    @Test
    public void testAddArrays() {

        double[] arr1 = {0.0, 2.0, 0.0, 4.0, 2.5};
        double[] arr2 = {1.0, 0.0, 3.0, 0.0, 2.5};
        double[] expected_result = {1.0, 2.0, 3.0, 4.0, 5.0};

        double[] result = ElementWise.addFirstToSecond(arr1, arr2);
        assertEquals(expected_result.length, result.length);

        for (int i = 0; i < result.length; i++)
            assertEquals(expected_result[i], result[i]);
    }

    @Test
    public void testSubtractArrays() {

        double[] arr1 = {0.0, 0.5, 1.0, 1.5, 2.0};
        double[] arr2 = {2.0, 2.0, 2.0, 2.0, 2.0};
        double[] expected_result = {2.0, 1.5, 1.0, 0.5, 0.0};

        double[] result = ElementWise.subtractFirstFromSecond(arr1, arr2);
        assertEquals(expected_result.length, result.length);

        for (int i = 0; i < result.length; i++)
            assertEquals(expected_result[i], result[i]);
    }
}
