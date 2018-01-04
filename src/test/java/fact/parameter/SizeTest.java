package fact.parameter;

import fact.features.Size;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertTrue;

/**
 * <fact.features.HillasAlpha distribution="dist" sourcePositionKey="sourcePositionKey" outputKey="alpha" />
 *
 * @author bruegge
 */
public class SizeTest extends ParameterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    final String outputKey = "sizeoutput";


    @Test
    public void testValidParameter() throws Exception {
//		//start processor with the correct parameter
        assertTrue("Expecteds output already in data item", !item.containsKey(outputKey));
        Size poser = new Size();
        poser.photonChargeKey = photonCharge;
        poser.pixelSetKey = shower;
        poser.outputKey = outputKey;
        poser.process(item);
        assertTrue("Expecteds output not in data item but it should be there", item.containsKey(outputKey));
//		item.remove(outputKey);
    }
}
