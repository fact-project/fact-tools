/**
 * 
 */
package fact.parameter;

import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;

/**
 * @author chris
 * 
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
