package fact.parameter;

import fact.features.DistributionFromShower;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertTrue;
/**
 * 
 * @author bruegge
 *
 */
public class DistributionFromShowerTest extends ParameterTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	final String shower="shower";

	final String outputKey = "distribution";

	@Test
	public void testValidParameter() throws Exception{
//		//start processor with the correct parameter
		assertTrue("Expecteds output already in data item", !item.containsKey(outputKey));
		DistributionFromShower poser = new DistributionFromShower();
		poser.setPixelSetKey(shower);
		poser.setWeightsKey(photonCharge);
		poser.setOutputKey(outputKey);
		poser.process(item);
		assertTrue("Expecteds output not in data item but it should be there", item.containsKey(outputKey));
//		item.remove(outputKey);
	}
}
