package fact.io;

import fact.filter.GpsTimeCorrection;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;

import static org.hamcrest.CoreMatchers.is;

/**
 * Created by Max Ahnen on 04.03.15.
 * <p>
 * This class shall test if gps time correction files
 * can be read properly. I supply one testfile
 * which is read by creating a GpsTimeCorrection class instance
 * and calling its setUrl(path) method.
 * <p>
 * In the end an assert tests if the
 * time correction is equal to the number in the file.
 */
public class GpsTimeCorrectionTest {

    @Test
    public void test() {

        //load stuff
        GpsTimeCorrection gtc = new GpsTimeCorrection();
        URL path = GpsTimeCorrectionTest.class.getResource("/gps_timerec_example.txt");
        gtc.setUrl(path);

        //check if OK
        Integer[] bufCorrectedTimes = gtc.gpsTimes.get(1);
        Assert.assertThat(bufCorrectedTimes[2], is(1403574123));
        Assert.assertThat(bufCorrectedTimes[3], is(370440));
    }

}


