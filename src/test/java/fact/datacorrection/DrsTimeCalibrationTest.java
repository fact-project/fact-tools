package fact.datacorrection;

import org.junit.Assert;
import org.junit.Test;
import stream.io.SourceURL;

import java.net.URL;

import static org.junit.Assert.fail;

/**
 * Created by Kai on 11.02.15.
 */
public class DrsTimeCalibrationTest {

    @Test
    public void loadFileTest() {
        try {
            DrsTimeCalibration drsTimeCalibration = new DrsTimeCalibration();
            URL resource = DrsTimeCalibration.class.getResource("/long_term_constants_median.time.drs.fits");

            drsTimeCalibration.setUrl(new SourceURL(resource));
            drsTimeCalibration.setDataKey("CellOffset");
            drsTimeCalibration.init(null);

        } catch (Exception e) {
            fail("Caught Exception while reading time calib file");
            e.printStackTrace();
        }
    }

    @Test
    public void constantsCalibrationSanity()  {
        try {
            DrsTimeCalibration drsTimeCalibration = new DrsTimeCalibration();
            URL resource = DrsTimeCalibration.class.getResource("/long_term_constants_median.time.drs.fits");

            drsTimeCalibration.setUrl(new SourceURL(resource));
            drsTimeCalibration.setDataKey("CellOffset");
            drsTimeCalibration.init(null);


            //this true_sampling_time array contains the most precise time of sampling
            // for each pixel. It contains 2048 values for each pixel, in order to
            // make accessing them easier.
            // One would usually access true_sampling_time[the_current_drs_chip_id][startcell:startcell+roi]
            // in order to work on pixel data.
            double[][] true_sampling_time = drsTimeCalibration.true_sampling_time;
            Assert.assertNotNull(true_sampling_time);

            // the true_sampling_time is measured in 'nominal_time_slices'. This means:
            // In a perfect world the true_sampling_time is identical with the sample index.
            // But in a real world the true_samplig_time slightly differs from the sample_index.
            // We have never seen them deviate by more than +-7 nominal slices.
            // So I simply assert here that they deviate by not more than 10.

            for(int patch = 0; patch < 160; patch++){
                for (int t = 0; t < true_sampling_time.length; t++) {
                    Assert.assertTrue(true_sampling_time[patch][t]-t <= 10);
                    Assert.assertTrue(true_sampling_time[patch][t]-t > -10);
                }
            }
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        }

    }
}
