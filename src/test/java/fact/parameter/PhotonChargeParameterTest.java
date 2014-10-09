package fact.parameter;

import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import stream.io.SourceURL;
import fact.extraction.PhotonCharge;
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
	public void testValidParameter() throws Exception {
		// //start processor with the correct parameter
		assertTrue("Expecteds output already in data item",
				!item.containsKey(outputKey));
		PhotonCharge poser = new PhotonCharge();
		poser.setDataKey(key);
		poser.setPositions(positions);
		poser.setOutputKey(outputKey);
		poser.setUrl(new SourceURL(FitsStreamTest.class
				.getResource("/defaultIntegralGains.csv")));
		poser.setRangeSearchWindow(25);
		poser.process(item);
		assertTrue("Expecteds output not in data item but it should be there",
				item.containsKey(outputKey));
		// item.remove(outputKey);
	}
}
