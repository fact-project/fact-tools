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
public class PhotonChargeParameterTest extends ParameterTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	final String key = "calib";
	final String outputKey = "photonchargeoutput";
	final String positions = "positions";

	@Test
	public void testMissingOutputKey() throws Exception{
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("outputKey");

		//start it with a missing parameter. forget outputkey
		PhotonCharge poser = new PhotonCharge();
		poser.setKey(key);
		poser.setPositions(positions);
//		poser.setOutputKey(outputKey);
		poser.init(null);
		poser.process(item);
	}
	
	@Test
	public void testMissingKey() throws Exception{
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("key");

		//start it with a missing parameter. forget outputkey
		PhotonCharge poser = new PhotonCharge();
//		poser.setKey(key);
		poser.setPositions(positions);
		poser.setOutputKey(outputKey);
		poser.init(null);
		poser.process(item);
	}
	
	@Test
	public void testMissingPositions() throws Exception{
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("positions");

		//start it with a missing parameter. forget outputkey
		PhotonCharge poser = new PhotonCharge();
		poser.setKey(key);
//		poser.setPositions(positions);
		poser.setOutputKey(outputKey);
		poser.init(null);
		poser.process(item);
	}

	@Test
	public void testValidParameter() throws Exception{
//		//start processor with the correct parameter
		assertTrue("Expecteds output already in data item", !item.containsKey(outputKey));
		PhotonCharge poser = new PhotonCharge();
		poser.setKey(key);
		poser.setPositions(positions);
		poser.setOutputKey(outputKey);
		poser.init(null);
		poser.process(item);
		assertTrue("Expecteds output not in data item but it should be there", item.containsKey(outputKey));
//		item.remove(outputKey);
	}


	@Test
	public void testNullPositions() throws Exception{
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("positions");

		//start it with a missing parameter. forget outputkey
		PhotonCharge poser = new PhotonCharge();
		poser.setKey(key);
		poser.setPositions(null);
		poser.setOutputKey(outputKey);
		poser.init(null);
		poser.process(item);
	}


}
