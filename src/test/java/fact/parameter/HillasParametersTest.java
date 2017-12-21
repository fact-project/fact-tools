package fact.parameter;

import fact.features.HillasParameters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertTrue;

/**
 * @author bruegge
 */
public class HillasParametersTest extends ParameterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    final String shower = "shower";

    final String outputKey = "distribution";

    @Test
    public void testValidParameter() throws Exception {
//		//start processor with the correct parameter
        assertTrue("Expecteds output already in data item", !item.containsKey(outputKey));
        HillasParameters poser = new HillasParameters();
        poser.pixelSetKey = shower;
        poser.weightsKey = photonCharge;
        poser.process(item);

        String[] keys = {"size", "width", "length", "cog", "cogX", "cogY", "delta", "m3Long", "m3Trans", "m4Long", "m4Trans", "skewnessLong", "skewnessTrans"};
        for (String key: keys){
            assertTrue("Expected output not in data item but it should be there", item.containsKey(key));
        }

//		item.remove(outputKey);
    }
}
