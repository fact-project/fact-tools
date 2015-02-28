package fact.parameter;

import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import stream.Data;
import stream.io.SourceURL;
import fact.cleaning.TwoLevelTimeMedian;
import fact.extraction.BasicExtraction;
import fact.extraction.RisingEdgeForPositions;
import fact.features.DistributionFromShower;
import fact.features.source.SourcePosition;
import fact.filter.DrsCalibration;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;

/**
 * <fact.features.HillasAlpha distribution="dist"
 * sourcePosition="sourcePosition" outputKey="alpha" />
 *
 * @author bruegge
 *
 */
public class ParameterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected FitsStream stream;
    protected Data item;
    final String distribution = "dist";
    final String sourcePosition = "pos";
    final String key = "calib";
    final String photonCharge = "photoncharge";
    final String positions = "positions";
    final String arrivalTime = "arrivalTime";
    final String shower = "shower";

    @Before
    public void setUp() throws Exception {
        URL dataUrl = FitsStreamTest.class.getResource("/testDataFile.fits.gz");
        SourceURL url = new SourceURL(dataUrl);

        stream = new FitsStream(url);

        try {
            stream.init();
            item = stream.read();
        } catch (Exception e) {
            fail("could not start stream with test file");
            e.printStackTrace();
        }

		URL drsUrl = FitsStreamTest.class
				.getResource("/testDrsFile.drs.fits.gz");
		DrsCalibration pr = new DrsCalibration();
		pr.setUrl(new SourceURL(drsUrl));
		pr.setOutputKey(key);
        pr.init(null);
		pr.process(item);
		
		BasicExtraction bE = new BasicExtraction();
		bE.setDataKey(key);
		bE.setOutputKeyMaxAmplPos(positions);
		bE.setOutputKeyPhotonCharge(photonCharge);
		bE.setUrl(new SourceURL(FitsStreamTest.class
				.getResource("/defaultIntegralGains.csv")));
		bE.process(item);
		
		RisingEdgeForPositions pR = new RisingEdgeForPositions();
		pR.setDataKey(key);
		pR.setAmplitudePositionsKey(positions);
		pR.setOutputKey(arrivalTime);
		pR.process(item);

		TwoLevelTimeMedian poser = new TwoLevelTimeMedian();
		poser.setPhotonChargeKey(photonCharge);
		poser.setArrivalTimeKey(arrivalTime);
		poser.setOutputKey(shower);
		poser.setCorePixelThreshold(1);
		poser.setNeighborPixelThreshold(0.1);
		poser.setMinNumberOfPixel(1);
		poser.setTimeLimit(40);
		poser.process(item);



        DistributionFromShower dist = new DistributionFromShower();
        dist.setShowerKey(shower);
        dist.setWeightsKey(photonCharge);
        dist.setOutputKey(distribution);
        dist.process(item);

        SourcePosition pos = new SourcePosition();
        pos.setX(0.0);
        pos.setY(0.0);
        pos.setOutputKey(sourcePosition);
        pos.init(null);
        pos.process(item);
    }

}
