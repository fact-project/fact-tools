package fact.parameter;

import fact.cleaning.CoreNeighborClean;
import fact.features.DistributionFromShower;
import fact.features.MaxAmplitudePosition;
import fact.features.PhotonCharge;
import fact.features.SourcePosition;
import fact.filter.DrsCalibration;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import stream.Data;
import stream.io.SourceURL;

import java.net.URL;

import static org.junit.Assert.fail;

/**
 * <fact.features.HillasAlpha distribution="dist" sourcePosition="sourcePosition" outputKey="alpha" />
 * @author bruegge
 *
 */
public class ParameterTest  {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	protected FitsStream stream;
	protected Data item;
	final String distribution = "dist";
	final String sourcePosition="pos";
	final String key = "calib";
    final String photonCharge = "photoncharge";
    final String positions = "positions";
    final String shower = "shower";



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
        pC.setDataKey(key);
        pC.setOutputKey(photonCharge);
		pC.setUrl(FitsStreamTest.class.getResource("/defaultIntegralGains.csv"));
		pC.setRangeSearchWindow(25);
        pC.setPositions(positions);
        pC.process(item);
        


        CoreNeighborClean poser = new CoreNeighborClean();
        poser.setKey(key);
        poser.setKeyPositions(positions);
        poser.setOutputKey(shower);
        poser.process(item);

        DistributionFromShower dist = new DistributionFromShower();
        dist.setPixel(shower);
        dist.setWeights(photonCharge);
        dist.setOutputKey(distribution);
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
