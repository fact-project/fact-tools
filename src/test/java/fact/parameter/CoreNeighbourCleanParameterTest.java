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
public class CoreNeighbourCleanParameterTest extends ParameterTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	final String key = "calib";
	final String outputKey = "cleanedEvent";
	final String positions = "positions";


	@Test
	public void testValidParameter() throws Exception{
//		//start processor with the correct parameter
		assertTrue("Expecteds output already in data item", !item.containsKey(outputKey));
		CoreNeighborClean poser = new CoreNeighborClean();
		poser.setKey(key);
		poser.setKeyPositions(positions);
		poser.setOutputKey(outputKey);
		poser.process(item);
		assertTrue("Expecteds output not in data item but it should be there", item.containsKey(outputKey));
//		item.remove(outputKey);
	}

}
