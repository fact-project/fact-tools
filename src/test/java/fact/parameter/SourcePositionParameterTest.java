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
import fact.features.SourcePosition;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;
/**
 * 
 * @author bruegge
 *
 */
public class SourcePositionParameterTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	FitsStream stream;
	Data item;
	final String outputKey = "pos";
	URL driveURL = SourcePositionParameterTest.class.getResource("/drive_file.fits");


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





	@Before
	public void setUp() {
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
	}

}
