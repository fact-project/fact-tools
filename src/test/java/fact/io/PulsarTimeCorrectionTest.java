package fact.io;

import fact.filter.PulsarTimeCorrection;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;

import static org.hamcrest.CoreMatchers.is;

/**
 * Created by Max Ahnen on 04.03.15.
 *
 * This class shall test if pulsar time correction files
 * can be read properly. I supply one testfile
 * which is read by creating a PulsarTimeCorrection class instance
 * and calling its setUrl(path) method.
 *
 * In the end an assert tests if the
 * time correction is equal to the number in the file.
 *
 */


public class PulsarTimeCorrectionTest {

    public static Double abs(Double a) {
        return (a <= 0.0D) ? 0.0D - a : a;
    }

    @Test
    public void test() {

        //load stuff
        PulsarTimeCorrection ptc = new PulsarTimeCorrection();
        URL path = PulsarTimeCorrectionTest.class.getResource("/p_example_tp");
        ptc.setUrl(path);

        //check if OK
        //Double[] bufCorrectedData1 = ptc.pulsarData.get( 13906 );
        Integer[] bufCorrectedTimes1 = ptc.pulsarTimes.get( 13906 );
        //Assert.assertThat( bufCorrectedData1[2]-0.2071059>0.00001,is(true));
        Assert.assertThat( bufCorrectedTimes1[0],is(1385694153) );//check if OK

        //Double[] bufCorrectedData2 = ptc.pulsarData.get( 13991 );
        Integer[] bufCorrectedTimes2 = ptc.pulsarTimes.get( 13991 );
        //Assert.assertThat( bufCorrectedData2[2]-0.0123076>0.00001,is(true));
        Assert.assertThat( bufCorrectedTimes2[1],is(537610) );
    }

}


