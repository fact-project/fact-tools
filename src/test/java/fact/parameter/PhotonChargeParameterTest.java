package fact.parameter;

import fact.extraction.BasicExtraction;
import fact.gainservice.GainService;
import fact.io.FITSStreamTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import stream.io.SourceURL;

import static org.junit.Assert.assertTrue;

/**
 * @author bruegge
 */
public class PhotonChargeParameterTest extends ParameterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    final String key = "calib";
    final String positions = "positions";
    final String outputKey = "photonchargeData";

    @Test
    public void testValidParameter() throws Exception {
        // //start processor with the correct parameter
        assertTrue("Expecteds output already in data item",
                !item.containsKey(outputKey));
        BasicExtraction extraction = new BasicExtraction();
        extraction.dataKey = key;
        extraction.outputKeyMaxAmplPos = positions;
        extraction.outputKeyPhotonCharge = outputKey;
        extraction.gainService = new GainService();
        extraction.process(item);
        assertTrue("Expecteds output not in data item but it should be there", item.containsKey(outputKey));
        // item.remove(outputKey);
    }
}
