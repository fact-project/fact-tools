package fact.parameter;

import fact.features.HillasAlpha;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertTrue;
/**
 * <fact.features.HillasAlpha distribution="dist" sourcePosition="sourcePosition" outputKey="alpha" />
 * @author bruegge
 *
 */
public class HillasAlphaTest extends ParameterTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	final String distribution = "dist";
	final String sourcePosition="pos";
	final String key = "calib";
	final String outputKey = "alpha";

	@Test
	public void testValidParameter() throws Exception{
//		//start processor with the correct parameter
		assertTrue("Expecteds output already in data item", !item.containsKey(outputKey));
		HillasAlpha poser = new HillasAlpha();
		poser.setDistribution(distribution);
		poser.setSourcePosition(sourcePosition);
		poser.setOutputKey(outputKey);
		poser.process(item);
		assertTrue("Expected output not in data item but it should be there", item.containsKey(outputKey));
//		item.remove(outputKey);
	}
}
