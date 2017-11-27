package fact.datacorrection;

import fact.container.PixelSet;
import fact.hexmap.FactPixelMapping;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by bruegge on 7/30/14.
 */
public class BadPixelInterpolationTest {

    int roi = 10;
    double[] mock = new double[1440 * roi];
    int[] badPixelChids = {2};

    @Test
    public void testInterpolation(){

        //set pixel with chid 2 in slice 0 to 60;
        mock[2 * roi] = 60;
        mock[3 * roi] = 1;

        InterpolateTimeSeries p = new InterpolateTimeSeries();
        mock = p.interpolateTimeSeries(mock, PixelSet.fromIDs(badPixelChids));
        assertTrue("Not interpolated correctly", mock[2 * roi] == 1.0/6.0);
    }

}
