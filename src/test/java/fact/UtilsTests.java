package fact;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by jebuss on 14.11.16.
 */
public class UtilsTests {
    @Test
    public void flattenEmpty2dArray(){

        double[][] empty = new double[0][0];
        double[] result = Utils.flatten2dArray(empty);
        Assert.assertEquals(result.length, 0);
    }


    @Test
    public void flattenSomeArrays(){

        final int num_arrays = 10;
        final int array_length = 2;
        double[][] someNumbers = new double[num_arrays][array_length];
        for (int i=0; i<num_arrays; i++) {
            for (int j=0; j<array_length; j++){
                someNumbers[i][j] = i*array_length + j;
            }
        }

        double[] result = Utils.flatten2dArray(someNumbers);

        Assert.assertEquals(result.length, num_arrays * array_length);
        for (int i=0; i<result.length; i++){
            Assert.assertEquals(result[i], (double)i);
        }
    }
}
