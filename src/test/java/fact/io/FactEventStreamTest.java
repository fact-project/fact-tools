/**
 * 
 */
package fact.io;

import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.runtime.ProcessContainer;

/**
 * @author chris
 * 
 */
public class FactEventStreamTest {

	static Logger log = LoggerFactory.getLogger(FactEventStreamTest.class);

	@Test
	public void testSpeed() {

		try {
			URL url = FactEventStreamTest.class
					.getResource("/fits-table-reader.xml");

			Long limit = 1000L;
			System.setProperty("limit", limit.toString());

			ProcessContainer container = new ProcessContainer(url);
			Long time = container.run();
			log.info("Container ran for {} ms.", time);

			Double seconds = time.doubleValue() / 1000.0d;

			log.info("Event rate: {} events/second", limit.doubleValue()
					/ seconds);

		} catch (Exception e) {
			e.printStackTrace();
			fail("Not yet implemented");
		}
	}
}
