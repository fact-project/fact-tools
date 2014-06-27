package fact.features;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SizeTest {


    private final String outputKey = "maxAmps";
    private double[] charges, zeroCharges;

    @Before
	public void setup() throws Exception{
        zeroCharges = new double[1440];
        charges =  new double[1440];
        //set all charges to 1.0
        for (int i = 0; i < charges.length; i++){
            charges[i] = 1.0;
        }
	}
	
    @Test
    public void simpleCharge() {
        int[] showerIds = {12,34,35,56,57,58,59,123,1322,1321,1320};
        Size size = new Size();
        double s = size.calculateSize(showerIds, charges);
        assertTrue("Size should be " + showerIds.length + " but its " + s , s == showerIds.length);
    }

    @Test
    public void zeroCharge() {
        int[] showerIds = {12,34,35,56,57,58,59,123,1322,1321,1320};
        Size size = new Size();
        double s = size.calculateSize(showerIds, zeroCharges);
        assertTrue("Size should be 0 but its " + s , s == 0);
    }
}
