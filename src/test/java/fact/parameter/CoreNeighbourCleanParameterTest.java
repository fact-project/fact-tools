package fact.parameter;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import stream.Data;
import stream.io.SourceURL;
import fact.cleaning.CoreNeighborClean;
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
public class CoreNeighbourCleanParameterTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	FitsStream stream;
	Data item;
	final String key = "calib";
	final String outputKey = "cleanedEvent";
	final String positions = "positions";

	final String charge = "charge";

	@Test
	public void testMissingOutputKey() throws Exception{
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("Missing");

		//start it with a missing parameter. forget outputkey
		CoreNeighborClean poser = new CoreNeighborClean();
		poser.setKey(key);
		poser.setKeyPositions(positions);
//		poser.setOutputKey(outputKey);
		poser.init(null);
		poser.process(item);
	}
	
	@Test
	public void testMissingKey() throws Exception{
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("Missing");

		//start it with a missing parameter. forget outputkey
		CoreNeighborClean poser = new CoreNeighborClean();
//		poser.setKey(key);
		poser.setKeyPositions(positions);
		poser.setOutputKey(outputKey);
		poser.init(null);
		poser.process(item);
	}
	
	@Test
	public void testMissingPositions() throws Exception{
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("Missing");

		//start it with a missing parameter. forget positions
		CoreNeighborClean poser = new CoreNeighborClean();
		poser.setKey(key);
//		poser.setKeyPositions(positions);
		poser.setOutputKey(outputKey);
		poser.init(null);
		poser.process(item);
	}

	@Test
	public void testValidParameter() throws Exception{
//		//start processor with the correct parameter
		assertTrue("Expecteds output already in data item", !item.containsKey(outputKey));
		CoreNeighborClean poser = new CoreNeighborClean();
		poser.setKey(key);
		poser.setKeyPositions(positions);
		poser.setOutputKey(outputKey);
		poser.init(null);
		poser.process(item);
		assertTrue("Expecteds output not in data item but it should be there", item.containsKey(outputKey));
//		item.remove(outputKey);
	}





	@Before
	public void setUp() throws Exception {
		URL dataUrl =  FitsStreamTest.class.getResource("/sample.fits.gz");
		SourceURL url = new SourceURL(dataUrl);

		stream = new FitsStream(url);

		try {
			stream.init();
			item = stream.read();
		} catch (Exception e) {
			fail("could not start stream with test file");
			e.printStackTrace();
		}
		
		URL drsUrl =  FitsStreamTest.class.getResource("/test.drs.fits.gz");
		DrsCalibration pr = new DrsCalibration();
		pr.setUrl(drsUrl.toString());
		pr.setOutputKey(key);
		pr.process(item);
		
		MaxAmplitudePosition pP = new MaxAmplitudePosition();
		pP.setKey(key);
		pP.setOutputKey(positions);
		pP.process(item);
		
		PhotonCharge pC = new PhotonCharge();
		pC.setKey(key);
		pC.setOutputKey(charge);
		pC.setPositions(positions);
		pC.init(null);
		pC.process(item);
		
	}

}
