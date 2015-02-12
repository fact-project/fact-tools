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
            URL resource = DrsTimeCalibrationTest.class.getResource("/testDrsTimeFile.time.drs.fits");

            drsTimeCalibration.setUrl(new SourceURL(resource));
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
            URL resource = DrsTimeCalibrationTest.class.getResource("/testDrsTimeFile.time.drs.fits");

            drsTimeCalibration.setUrl(new SourceURL(resource));
            drsTimeCalibration.init(null);


            //these constant
            double[][] constants = drsTimeCalibration.samplingConstants;
            Assert.assertNotNull(constants);

            for(int patch = 0; patch < 160; patch++){
                for (int t = 0; t < constants.length; t++) {
                    Assert.assertTrue(constants[patch][t] < 2060);
                    Assert.assertTrue(constants[patch][t] > -10);
                }
            }
        } catch (Exception e) {
            fail();
            e.printStackTrace();
        }

    }
}
