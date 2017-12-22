package fact.parameter;

import fact.cleaning.TwoLevelTimeMedian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertTrue;

/**
 * @author bruegge
 */
public class CoreNeighbourCleanParameterTest extends ParameterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    final String outputKey = "cleanedEvent";


    @Test
    public void testValidParameter() throws Exception {
//		//start processor with the correct parameter
        assertTrue("Expecteds output already in data item", !item.containsKey(outputKey));
        TwoLevelTimeMedian poser = new TwoLevelTimeMedian();
        poser.calibService = calibService;
        poser.photonChargeKey = photonCharge;
        poser.arrivalTimeKey = arrivalTime;
        poser.corePixelThreshold = 0;
        poser.neighborPixelThreshold = 0;
        poser.minNumberOfPixel = 0;
        poser.timeLimit = 300;
        poser.outputKey = outputKey;
        poser.process(item);
        assertTrue("Expecteds output not in data item but it should be there", item.containsKey(outputKey));
//		item.remove(outputKey);
    }

}
