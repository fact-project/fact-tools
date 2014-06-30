package fact.parameter;

import fact.features.SourcePosition;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URL;

import static org.junit.Assert.assertTrue;
/**
 * 
 * @author bruegge
 *
 */
public class SourcePositionParameterTest extends ParameterTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	final String outputKey = "pos";
	URL driveURL = SourcePositionParameterTest.class.getResource("/testDriveFile.fits");


	@Test
	public void testMissingPhysicalSourceParameter() throws Exception{
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("physicalSource");
		//start it with a missing parameter. forget physical position
		SourcePosition poser = new SourcePosition();
		poser.setUrl(driveURL);
		//		poser.setPhysicalSource("crab");
		poser.setOutputKey(outputKey);
		poser.init(null);
		poser.process(item);
	}

	@Test
	public void testInvalidPhysicalSourceParameter() throws Exception{
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("physicalSource unknown");
		SourcePosition poser = new SourcePosition();
		poser.setUrl(driveURL);
		poser.setPhysicalSource("blabla not exisitng source");
		poser.setOutputKey(outputKey);
		poser.init(null);
		poser.process(item);
	}

	@Test
	public void testMissingOutputKey() throws Exception{
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("outputKey");

		//start it with a missing parameter. forget outputkey
		SourcePosition poser = new SourcePosition();
		poser.setUrl(driveURL);
		poser.setPhysicalSource("crab");
		//		poser.setOutputKey(outputKey);
		poser.init(null);
		poser.process(item);
	}

	@Test
	public void testValidParameter() throws Exception{
		//start processor with the correct parameter
		SourcePosition poser = new SourcePosition();
		poser.setUrl(driveURL);
		poser.setPhysicalSource("crab");
		poser.setOutputKey(outputKey);
		poser.init(null);
		poser.process(item);
		assertTrue("Expecteds output not in data item but it should be there", item.containsKey(outputKey));
		item.remove(outputKey);
	}
}
