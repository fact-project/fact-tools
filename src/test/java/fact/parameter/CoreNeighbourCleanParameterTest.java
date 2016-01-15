package fact.parameter;

import fact.cleaning.TwoLevelTimeMedian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertTrue;
/**
 * 
 * @author bruegge
 *
 */
public class CoreNeighbourCleanParameterTest extends ParameterTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	final String outputKey = "cleanedEvent";


	@Test
	public void testValidParameter() throws Exception{
//		//start processor with the correct parameter
		assertTrue("Expecteds output already in data item", !item.containsKey(outputKey));
		TwoLevelTimeMedian poser = new TwoLevelTimeMedian();
		poser.setCalibService(calibService);
		poser.setPhotonChargeKey(photonCharge);
		poser.setArrivalTimeKey(arrivalTime);
        poser.setCorePixelThreshold(0);
        poser.setNeighborPixelThreshold(0);
        poser.setMinNumberOfPixel(0);
        poser.setTimeLimit(300);
		poser.setOutputKey(outputKey);
		poser.process(item);
		assertTrue("Expecteds output not in data item but it should be there", item.containsKey(outputKey));
//		item.remove(outputKey);
	}

}
