package fact.features;

import fact.Utils;
import junit.framework.Assert;
import org.junit.Test;
import fact.features.singlePulse.timeLineExtraction.ElementWise;

public class ElementWiseTest {

    @Test
    public void testMultiplyEmpty(){

        double[] empty = {};

        double[] result = ElementWise.multiply(empty, 1.0);
        Assert.assertEquals(0, empty.length);
        Assert.assertEquals(0, result.length);
    }

    @Test
    public void testMultiplyOnes(){

        double[] ones = {1.0, 1.0, 1.0, 1.0, 1.0};

        double[] result = ElementWise.multiply(ones, 1.0);
        Assert.assertEquals(ones.length, result.length);

        for(int i=0; i<ones.length; i++)
            Assert.assertEquals(1.0, result[i]);
    }

    @Test
    public void testMultiplyArrayOnesSeveralScalars(){

        double[] ones = {1.0, 1.0, 1.0, 1.0, 1.0};

        for(int i=-13; i<37; i++) {
            double scalar = (double)(i);
            double[] result = ElementWise.multiply(ones, scalar);
            Assert.assertEquals(ones.length, result.length);

            for(int j=0; j<ones.length; j++)
                Assert.assertEquals(scalar, result[j]);       
        }
    }

    @Test
    public void testAddEmpty(){

        double[] empty = {};

        double[] result = ElementWise.add(empty, 1.0);
        Assert.assertEquals(0, empty.length);
        Assert.assertEquals(0, result.length);
    }

    @Test
    public void testAddZeros(){

        double[] zeros = {0.0, 0.0, 0.0, 0.0, 0.0};

        double[] result = ElementWise.add(zeros, 1.0);
        Assert.assertEquals(5, zeros.length);
        Assert.assertEquals(5, result.length);

        for(int i=0; i<zeros.length; i++)
            Assert.assertEquals(1.0, result[i]);
    }
}
