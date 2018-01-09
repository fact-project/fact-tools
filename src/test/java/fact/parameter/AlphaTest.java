package fact.parameter;

import fact.features.source.Alpha;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertTrue;

/**
 * <fact.features.HillasAlpha distribution="dist" sourcePositionKey="sourcePositionKey" outputKey="alpha" />
 *
 * @author bruegge
 */
public class AlphaTest extends ParameterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    final String cogKey = "cog";
    final String deltaKey = "delta";
    final String sourcePositionKey = "pos";
    final String outputKey = "alpha";

    @Test
    public void testValidParameter() throws Exception {
//		//start processor with the correct parameter
        assertTrue("Expecteds output already in data item", !item.containsKey(outputKey));
        Alpha poser = new Alpha();
        poser.cogKey = cogKey;
        poser.deltaKey = deltaKey;
        poser.sourcePositionKey = sourcePositionKey;
        poser.outputKey = outputKey;

        poser.process(item);
        assertTrue("Expected output not in data item but it should be there", item.containsKey(outputKey));
//		item.remove(outputKey);
    }
}
