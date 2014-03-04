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
import fact.features.MaxAmplitudePosition;
import fact.features.PhotonCharge;
import fact.filter.DrsCalibration;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;
/**
 * 
 * @author bruegge
 *
 */
public class DistributionFromShowerTest extends ParameterTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	final String shower="shower";
	final String wheights = "charge";

	final String outputKey = "distribution";

	@Test
	public void testMissingOutputKey() throws Exception{
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("Missing");

		//start it with a missing parameter. forget outputkey
		DistributionFromShower poser = new DistributionFromShower();
		poser.setKey(shower);
		poser.setWeights(wheights);
//		poser.setOutputKey(outputKey);
		poser.init(null);
		poser.process(item);
	}
	
	@Test
	public void testMissinWheights() throws Exception{
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("Missing");

		DistributionFromShower poser = new DistributionFromShower();
		poser.setKey(shower);
		//poser.setWeights(wheights);
		poser.setOutputKey(outputKey);
		poser.init(null);
		poser.process(item);
	}
	
	@Test
	public void testMissingKey() throws Exception{
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("Missing");

		DistributionFromShower poser = new DistributionFromShower();
		//poser.setKey(shower);
		poser.setWeights(wheights);
		poser.setOutputKey(outputKey);
		poser.init(null);
		poser.process(item);
	}


	@Test
	public void testValidParameter() throws Exception{
//		//start processor with the correct parameter
		assertTrue("Expecteds output already in data item", !item.containsKey(outputKey));
		DistributionFromShower poser = new DistributionFromShower();
		poser.setKey(shower);
		poser.setWeights(wheights);
		poser.setOutputKey(outputKey);
		poser.init(null);
		poser.process(item);
		assertTrue("Expecteds output not in data item but it should be there", item.containsKey(outputKey));
//		item.remove(outputKey);
	}
}
