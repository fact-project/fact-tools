package fact.parameter;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import stream.Data;
import stream.io.SourceURL;
import fact.cleaning.CoreNeighborClean;
import fact.features.DistributionFromShower;
import fact.features.HillasAlpha;
import fact.features.MaxAmplitudePosition;
import fact.features.PhotonCharge;
import fact.features.SourcePosition;
import fact.filter.DrsCalibration;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;
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
	final String outputKey = "alpha";

	@Test
	public void testMissingOutputKey() throws Exception{
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("Missing parameter: outputKey");

		//start it with a missing parameter. forget outputkey
		HillasAlpha poser = new HillasAlpha();
		poser.setDistribution(distribution);
		poser.setSourcePosition(sourcePosition);
//		poser.setOutputKey(outputKey);
		poser.process(item);
	}
	
	
	@Test
	public void testMissingDistribution() throws Exception{
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("Missing parameter: distribution");

		HillasAlpha poser = new HillasAlpha();
		//poser.setDistribution(distribution);
		poser.setSourcePosition(sourcePosition);
		poser.setOutputKey(outputKey);
		poser.process(item);
	}


	@Test
	public void testValidParameter() throws Exception{
//		//start processor with the correct parameter
		assertTrue("Expecteds output already in data item", !item.containsKey(outputKey));
		HillasAlpha poser = new HillasAlpha();
		poser.setDistribution(distribution);
		poser.setSourcePosition(sourcePosition);
		poser.setOutputKey(outputKey);
		poser.process(item);
		assertTrue("Expecteds output not in data item but it should be there", item.containsKey(outputKey));
//		item.remove(outputKey);
	}
}
