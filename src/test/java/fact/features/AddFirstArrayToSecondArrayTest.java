package fact.features;

import fact.photonstream.timeSeriesExtraction.AddFirstArrayToSecondArray;
import junit.framework.Assert;
import org.junit.Test;

public class AddFirstArrayToSecondArrayTest {

    @Test
    public void testAddArraysAddBothEmpty() {

        double[] empty1 = {};
        double[] empty2 = {};
        int slice = 0;

        Assert.assertEquals(0, empty1.length);
        Assert.assertEquals(0, empty2.length);

        AddFirstArrayToSecondArray.at(empty1, empty2, slice);

        Assert.assertEquals(0, empty1.length);
        Assert.assertEquals(0, empty2.length);
    }

    @Test
    public void testAddArraysAddSecondEmpty() {

        double[] first = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0};
        double[] second = {};
        int slice = 0;

        Assert.assertEquals(6, first.length);
        Assert.assertEquals(0, second.length);

        AddFirstArrayToSecondArray.at(first, second, slice);

        Assert.assertEquals(6, first.length);
        Assert.assertEquals(0, second.length);

        Assert.assertEquals(0.0, first[0]);
        Assert.assertEquals(1.0, first[1]);
        Assert.assertEquals(2.0, first[2]);
        Assert.assertEquals(3.0, first[3]);
        Assert.assertEquals(4.0, first[4]);
        Assert.assertEquals(5.0, first[5]);
    }

    @Test
    public void testAddArraysAddFirstEmpty() {

        double[] first = {};
        double[] second = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0};
        int slice = 0;

        Assert.assertEquals(0, first.length);
        Assert.assertEquals(6, second.length);

        AddFirstArrayToSecondArray.at(first, second, slice);

        Assert.assertEquals(0, first.length);
        Assert.assertEquals(6, second.length);

        Assert.assertEquals(0.0, second[0]);
        Assert.assertEquals(1.0, second[1]);
        Assert.assertEquals(2.0, second[2]);
        Assert.assertEquals(3.0, second[3]);
        Assert.assertEquals(4.0, second[4]);
        Assert.assertEquals(5.0, second[5]);
    }

    @Test
    public void testAddArraysAddBothZeros() {

        double[] first = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        double[] second = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        int slice = 0;

        Assert.assertEquals(7, first.length);
        Assert.assertEquals(10, second.length);

        AddFirstArrayToSecondArray.at(first, second, slice);

        Assert.assertEquals(7, first.length);
        Assert.assertEquals(10, second.length);

        for (int i = 0; i < first.length; i++)
            Assert.assertEquals(0.0, first[i]);
        for (int i = 0; i < second.length; i++)
            Assert.assertEquals(0.0, second[i]);
    }

    @Test
    public void testAddArraysFirstZerosSignalInSecond() {

        double[] first = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        double[] second = new double[10];
        for (int i = 0; i < second.length; i++)
            second[i] = (double) (i);
        int slice = 0;

        Assert.assertEquals(7, first.length);
        Assert.assertEquals(10, second.length);

        AddFirstArrayToSecondArray.at(first, second, slice);

        Assert.assertEquals(7, first.length);
        Assert.assertEquals(10, second.length);

        for (int i = 0; i < first.length; i++)
            Assert.assertEquals(0.0, first[i]);
        for (int i = 0; i < second.length; i++)
            Assert.assertEquals((double) (i), second[i]);
    }

    @Test
    public void testAddArraysFirstSimpleSignalSecondZeros() {

        for (int slice = 0; slice < 10; slice++) {
            double[] first = {1.0};
            double[] second = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

            AddFirstArrayToSecondArray.at(first, second, slice);

            Assert.assertEquals(1.0, first[0]);

            for (int i = 0; i < second.length; i++)
                if (i == slice)
                    Assert.assertEquals(1.0, second[i]);
                else
                    Assert.assertEquals(0.0, second[i]);
        }
    }

    @Test
    public void testAddArraysSeveralTimesFirstSimpleSignalSecondZeros() {

        double[] first = {1.0};
        double[] second = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

        for (int slice = 0; slice < 10; slice++)
            AddFirstArrayToSecondArray.at(first, second, slice);

        Assert.assertEquals(1.0, first[0]);

        for (int i = 0; i < second.length; i++)
            Assert.assertEquals(1.0, second[i]);
    }

    @Test
    public void testAddArraysAtNegative() {

        double[] first = {1.0, 1.0};
        double[] second = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

        int atSlice = -1;

        AddFirstArrayToSecondArray.at(first, second, atSlice);

        Assert.assertEquals(1.0, second[0]);
        Assert.assertEquals(0.0, second[1]);
        Assert.assertEquals(0.0, second[2]);
        Assert.assertEquals(0.0, second[3]);
        Assert.assertEquals(0.0, second[4]);
        Assert.assertEquals(0.0, second[5]);
        Assert.assertEquals(0.0, second[6]);
    }

    @Test
    public void testAddArraysAtNegativeFullOverlap() {

        double[] first = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
        double[] second = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

        int atSlice = -1;

        AddFirstArrayToSecondArray.at(first, second, atSlice);

        Assert.assertEquals(2.0, second[0]);
        Assert.assertEquals(3.0, second[1]);
        Assert.assertEquals(4.0, second[2]);
        Assert.assertEquals(5.0, second[3]);
        Assert.assertEquals(6.0, second[4]);
        Assert.assertEquals(7.0, second[5]);
        Assert.assertEquals(8.0, second[6]);
    }
}
