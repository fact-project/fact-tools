package fact.parameter;

import fact.cleaning.CoreNeighborClean;
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
		CoreNeighborClean poser = new CoreNeighborClean();
		poser.setPhotonChargeKey(photonCharge);
		poser.setArrivalTimeKey(positions);
        poser.setCorePixelThreshold(0);
        poser.setNeighborPixelThreshold(0);
        poser.setMinNumberOfPixel(0);
        poser.setTimeThreshold(300);
		poser.setOutputKey(outputKey);
		poser.process(item);
		assertTrue("Expecteds output not in data item but it should be there", item.containsKey(outputKey));
//		item.remove(outputKey);
	}

}
