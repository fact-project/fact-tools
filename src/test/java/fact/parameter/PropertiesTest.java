/**
 *
 */
package fact.parameter;

import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.fail;

/**
 * @author chris
 */
public class PropertiesTest {

    @Test
    public void test() {
        try {
            URL url = PropertiesTest.class.getResource("/properties-test.xml");
            stream.run.main(url);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test failed: " + e.getMessage());
        }
    }
}
