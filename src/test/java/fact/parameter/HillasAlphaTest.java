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
public class HillasAlphaTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	FitsStream stream;
	Data item;
	final String distribution = "dist";
	final String sourcePosition="pos";
	final String key = "calib";
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
		pP.setOutputKey("positions");
		pP.process(item);
		
		PhotonCharge pC = new PhotonCharge();
		pC.setKey(key);
		pC.setOutputKey("charge");
		pC.setPositions("positions");
		pC.init(null);
		pC.process(item);
		
		
		CoreNeighborClean poser = new CoreNeighborClean();
		poser.setKey(key);
		poser.setKeyPositions("positions");
		poser.setOutputKey("shower");
		poser.init(null);
		poser.process(item);
		
		DistributionFromShower dist = new DistributionFromShower();
		dist.setKey("shower");
		dist.setWeights("charge");
		dist.setOutputKey(distribution);
		dist.init(null);
		dist.process(item);
		
		URL driveURL = FitsStreamTest.class.getResource("/drive_file.fits");
		SourcePosition pos = new SourcePosition();
		pos.setUrl(driveURL);
		pos.setPhysicalSource("crab");
		pos.setOutputKey(sourcePosition);
		pos.init(null);
		pos.process(item);
	}

}
