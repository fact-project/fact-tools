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

}
