package fact.TriggerEmulation;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jbuss on 15.11.17.
 */
public class SummationTest {

    int npix = 9;
    int roi = 300;

    @Test
    public void testPixelSummation(){
        double[][] pixelData = generateTestData();
        SumUpPatches sumUpPatches = new SumUpPatches();
        double[] sum = sumUpPatches.sumPixelsOfPatch(pixelData, 0);
        double[] result = generateExpectedResult(0,0);
        Assert.assertArrayEquals(result, sum, Double.MIN_NORMAL);
    }

    private double[][] generateTestData() {
        double[][] pixelData = new double[9][];
        for (int i = 0; i < 9; i++) {
            pixelData[i] = new double[roi];
            for (int j = 0; j < roi; j++) {
                pixelData[i][j]= ((double)j);
            }
        }
        return pixelData;
    }

    private double[] generateExpectedResult(int skipFirst, int skipLast) {
        double[] pixelData = new double[roi];

        for (int j = skipFirst; j < roi-skipLast; j++) {
            pixelData[j]= j*9;
        }
        return pixelData;
    }


    @Test
    public void testToFullROIDataArrayLength(){
        double[][] pixelData = {{2, 3},{2, 3}};
        SumUpPatches sumUpPatches = new SumUpPatches();
        double[] sum = sumUpPatches.toDataArray(pixelData);

        double[] result = new double[0];
        double[] template = {2, 3, 2, 3};
        for (int i = 0; i < 9; i++) {
            result = ArrayUtils.addAll(result, template);
        }
        Assert.assertArrayEquals(result, sum, Double.MIN_NORMAL);
    }
}
