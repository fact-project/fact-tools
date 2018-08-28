package fact.features;

import fact.Constants;
import fact.container.PixelSet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SizeTest {


    private final String outputKey = "maxAmps";
    private double[] charges, zeroCharges;

    @Before
    public void setup() throws Exception {
        zeroCharges = new double[Constants.N_PIXELS];
        charges = new double[Constants.N_PIXELS];
        //set all charges to 1.0
        for (int i = 0; i < charges.length; i++) {
            charges[i] = 1.0;
        }
    }

    @Test
    public void simpleCharge() {
        int[] showerIds = {12, 34, 35, 56, 57, 58, 59, 123, 1322, 1321, 1320};
        Size size = new Size();
        double s = size.calculateSize(PixelSet.fromIDs(showerIds), charges);
        assertTrue("Size should be " + showerIds.length + " but its " + s, s == showerIds.length);
    }

    @Test
    public void zeroCharge() {
        int[] showerIds = {12, 34, 35, 56, 57, 58, 59, 123, 1322, 1321, 1320};
        Size size = new Size();
        double s = size.calculateSize(PixelSet.fromIDs(showerIds), zeroCharges);
        assertTrue("Size should be 0 but its " + s, s == 0);
    }
}
