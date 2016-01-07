package fact.features;

import fact.parameter.ParameterTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by kai on 06.01.16.
 */
public class HoughTrafoTest extends ParameterTest {
    static Logger log = LoggerFactory.getLogger(HoughTrafoTest.class);



    @Rule
    public ExpectedException thrown = ExpectedException.none();

    final String distribution = "dist";

    @Test
    public void testValidParameter() throws Exception{
        MuonHoughTransform poser = new MuonHoughTransform();
        poser.setPixelKey(distribution);
        poser.setPhotonChargeKey("photoncharge");
        poser.process(item);
    }


    /**
     * Estimate the size of the data structures build during initilizaiotn of the hough trafo processor
     */
    @Test
    public void testMemoryConsumption() throws Exception {
        MuonHoughTransform transform = new MuonHoughTransform();
        transform.init(null);

        long size = 0;
        for( ArrayList<Integer> entry : transform.circle2chids.values()){
            size += entry.size();
        }
        log.info("Moun circle to chid contains {} Integer entries. Thats about {} MebiByte", size, (size*192/8)/(1024*1024));

        long circles = 0;
        for (ArrayList<int[]> l : transform.chid2circles){
            circles += l.size();
        }
        log.info("chid2circles contains {} primitive integers. Thats about {} MebiByte", circles*3, (circles*3*64/8)/((1024*1024)) );

    }
}
