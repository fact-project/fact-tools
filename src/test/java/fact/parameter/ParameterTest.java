package fact.parameter;

import fact.calibrationservice.ConstantCalibService;
import fact.cleaning.TwoLevelTimeMedian;
import fact.datacorrection.DrsCalibration;
import fact.datacorrection.InterpolatePixelArray;
import fact.extraction.BasicExtraction;
import fact.extraction.RisingEdgeForPositions;
import fact.features.HillasParameters;
import fact.features.source.SourcePosition;
import fact.gainservice.GainService;
import fact.io.FITSStreamTest;
import fact.io.hdureader.FITSStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import stream.Data;
import stream.io.SourceURL;

import java.net.URL;

import static org.junit.Assert.fail;

/**
 * <fact.features.HillasAlpha distribution="dist"
 * sourcePositionKey="sourcePositionKey" outputKey="alpha" />
 *
 * @author bruegge
 */
public class ParameterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected FITSStream stream;
    protected Data item;
    final String distribution = "dist";
    final String sourcePosition = "pos";
    final String key = "calib";
    final String photonCharge = "photoncharge";
    final String positions = "positions";
    final String arrivalTime = "arrivalTime";
    final String shower = "shower";
    final ConstantCalibService calibService = new ConstantCalibService();

    @Before
    public void setUp() throws Exception {
        URL dataUrl = FITSStreamTest.class.getResource("/testDataFile.fits.gz");
        SourceURL url = new SourceURL(dataUrl);


        stream = new FITSStream(url);

        try {
            stream.init();
            item = stream.read();
        } catch (Exception e) {
            e.printStackTrace();
            fail("could not start stream with test file");
        }

        URL drsUrl = FITSStreamTest.class.getResource("/testDrsFile.drs.fits.gz");
        DrsCalibration pr = new DrsCalibration();
        pr.url = drsUrl.toString();
        pr.outputKey = key;
        pr.init(null);
        pr.process(item);

        BasicExtraction bE = new BasicExtraction();
        bE.dataKey = key;
        bE.outputKeyMaxAmplPos = positions;
        bE.outputKeyPhotonCharge = photonCharge;
        bE.gainService = new GainService();
        bE.process(item);

        InterpolatePixelArray interpolatePhotoncharge = new InterpolatePixelArray();
        interpolatePhotoncharge.calibService = new ConstantCalibService();
        interpolatePhotoncharge.inputKey = "photoncharge";
        interpolatePhotoncharge.outputKey = "photoncharge";
        interpolatePhotoncharge.process(item);

        RisingEdgeForPositions pR = new RisingEdgeForPositions();
        pR.dataKey = key;
        pR.amplitudePositionsKey = positions;
        pR.outputKey = arrivalTime;
        pR.process(item);

        TwoLevelTimeMedian poser = new TwoLevelTimeMedian();
        poser.calibService = calibService;
        poser.photonChargeKey = photonCharge;
        poser.arrivalTimeKey = arrivalTime;
        poser.outputKey = shower;
        poser.corePixelThreshold = 1;
        poser.neighborPixelThreshold = 0.1;
        poser.minNumberOfPixel = 1;
        poser.timeLimit = 40;
        poser.process(item);


        HillasParameters dist = new HillasParameters();
        dist.pixelSetKey = shower;
        dist.weightsKey = photonCharge;
        dist.process(item);

        SourcePosition pos = new SourcePosition();
        pos.x = 0.0;
        pos.y = 0.0;
        pos.outputKey = sourcePosition;
        pos.init(null);
        pos.process(item);
    }

}
