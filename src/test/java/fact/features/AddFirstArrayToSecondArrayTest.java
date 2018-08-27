package fact.features;

import fact.photonstream.timeSeriesExtraction.AddFirstArrayToSecondArray;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AddFirstArrayToSecondArrayTest {

    private double eps = 1e-12;

    @Test
    public void testAddArraysAddBothEmpty() {

        double[] empty1 = {};
        double[] empty2 = {};
        int slice = 0;

        assertEquals(0, empty1.length);
        assertEquals(0, empty2.length);

        AddFirstArrayToSecondArray.at(empty1, empty2, slice);

        assertEquals(0, empty1.length);
        assertEquals(0, empty2.length);
    }

    @Test
    public void testAddArraysAddSecondEmpty() {

        double[] first = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0};
        double[] second = {};
        int slice = 0;

        assertEquals(6, first.length);
        assertEquals(0, second.length);

        AddFirstArrayToSecondArray.at(first, second, slice);

        assertEquals(6, first.length);
        assertEquals(0, second.length);

        assertEquals(0.0, first[0], eps);
        assertEquals(1.0, first[1], eps);
        assertEquals(2.0, first[2], eps);
        assertEquals(3.0, first[3], eps);
        assertEquals(4.0, first[4], eps);
        assertEquals(5.0, first[5], eps);
    }

    @Test
    public void testAddArraysAddFirstEmpty() {

        double[] first = {};
        double[] second = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0};
        int slice = 0;

        assertEquals(0, first.length);
        assertEquals(6, second.length);

        AddFirstArrayToSecondArray.at(first, second, slice);

        assertEquals(0, first.length);
        assertEquals(6, second.length);

        assertEquals(0.0, second[0], eps);
        assertEquals(1.0, second[1], eps);
        assertEquals(2.0, second[2], eps);
        assertEquals(3.0, second[3], eps);
        assertEquals(4.0, second[4], eps);
        assertEquals(5.0, second[5], eps);
    }

    @Test
    public void testAddArraysAddBothZeros() {

        double[] first = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        double[] second = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        int slice = 0;

        assertEquals(7, first.length);
        assertEquals(10, second.length);

        AddFirstArrayToSecondArray.at(first, second, slice);

        assertEquals(7, first.length);
        assertEquals(10, second.length);

        for (int i = 0; i < first.length; i++)
            assertEquals(0.0, first[i], eps);
        for (int i = 0; i < second.length; i++)
            assertEquals(0.0, second[i], eps);
    }

    @Test
    public void testAddArraysFirstZerosSignalInSecond() {

        double[] first = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        double[] second = new double[10];
        for (int i = 0; i < second.length; i++)
            second[i] = (double) (i);
        int slice = 0;

        assertEquals(7, first.length);
        assertEquals(10, second.length);

        AddFirstArrayToSecondArray.at(first, second, slice);

        assertEquals(7, first.length);
        assertEquals(10, second.length);

        for (int i = 0; i < first.length; i++)
            assertEquals(0.0, first[i], eps);
        for (int i = 0; i < second.length; i++)
            assertEquals((double) (i), second[i], eps);
    }

    @Test
    public void testAddArraysFirstSimpleSignalSecondZeros() {

        for (int slice = 0; slice < 10; slice++) {
            double[] first = {1.0};
            double[] second = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

            AddFirstArrayToSecondArray.at(first, second, slice);

            assertEquals(1.0, first[0], eps);

            for (int i = 0; i < second.length; i++)
                if (i == slice)
                    assertEquals(1.0, second[i], eps);
                else
                    assertEquals(0.0, second[i], eps);
        }
    }

    @Test
    public void testAddArraysSeveralTimesFirstSimpleSignalSecondZeros() {

        double[] first = {1.0};
        double[] second = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

        for (int slice = 0; slice < 10; slice++)
            AddFirstArrayToSecondArray.at(first, second, slice);

        assertEquals(1.0, first[0], eps);

        for (int i = 0; i < second.length; i++)
            assertEquals(1.0, second[i], eps);
    }

    @Test
    public void testAddArraysAtNegative() {

        double[] first = {1.0, 1.0};
        double[] second = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

        int atSlice = -1;

        AddFirstArrayToSecondArray.at(first, second, atSlice);

        assertEquals(1.0, second[0], eps);
        assertEquals(0.0, second[1], eps);
        assertEquals(0.0, second[2], eps);
        assertEquals(0.0, second[3], eps);
        assertEquals(0.0, second[4], eps);
        assertEquals(0.0, second[5], eps);
        assertEquals(0.0, second[6], eps);
    }

    @Test
    public void testAddArraysAtNegativeFullOverlap() {

        double[] first = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
        double[] second = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

        int atSlice = -1;

        AddFirstArrayToSecondArray.at(first, second, atSlice);

        assertEquals(2.0, second[0], eps);
        assertEquals(3.0, second[1], eps);
        assertEquals(4.0, second[2], eps);
        assertEquals(5.0, second[3], eps);
        assertEquals(6.0, second[4], eps);
        assertEquals(7.0, second[5], eps);
        assertEquals(8.0, second[6], eps);
    }
}
