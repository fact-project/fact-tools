package fact;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by jebuss on 14.11.16.
 */
public class UtilsTests {
    @Test
    public void flattenEmpty2dArray(){

        double[][] empty = new double[0][0];
        double[] result = Utils.flatten2dArray(empty);
        assertEquals(result.length, 0);
    }

}
