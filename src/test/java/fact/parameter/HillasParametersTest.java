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

    @Test
    public void testValidParameter() throws Exception {
        // Start processor with the correct parameter

        HillasParameters poser = new HillasParameters();
        poser.pixelSetKey = shower;
        poser.weightsKey = photonCharge;
        poser.process(item);

        String[] keys = {"size", "width", "length", "cog", "cog_x", "cog_y", "delta", "m3_long", "m3_trans", "m4_long", "m4_trans", "skewness_long", "skewness_trans"};
        for (String key: keys){
            assertTrue("Expected output not in data item but it should be there", item.containsKey(key));
        }

    }
}
