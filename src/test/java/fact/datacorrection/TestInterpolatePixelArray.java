package fact.datacorrection;

import fact.Constants;
import fact.container.PixelSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by bruegge on 7/30/14.
 */
public class TestInterpolatePixelArray {

    double[] mock = new double[Constants.N_PIXELS];


    @Test
    public void testInterpolation(){

        //set pixel with chid 2 in slice 0 to 60;
        mock[2] = 60;
        mock[5] = 1;
        mock[3] = 1;

        InterpolatePixelArray p = new InterpolatePixelArray();

        mock = p.interpolatePixelArray(mock, PixelSet.fromIDs(new int[] {2}));
        assertEquals("Not interpolated correctly", 2.0/6.0, mock[2], 1e-16);
    }


    @Test
    public void testInterpolationThrows(){

        //set pixel with chid 2 in slice 0 to 60;
        mock[2] = 60;
        mock[5] = 1;
        mock[3] = 1;

        InterpolatePixelArray p = new InterpolatePixelArray();
        try {
            mock = p.interpolatePixelArray(mock, PixelSet.fromIDs(new int[]{0, 2, 3, 17, 32}));
            fail("InterpolatePixelArray did not throw to few pixels error");
        } catch (RuntimeException e) {

        }
    }

}
