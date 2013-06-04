/**
 * 
 */
package fact.io;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import stream.Data;
import stream.io.SourceURL;

/**
 * @author chris
 * 
 */
public class WeatherTest {

	static Logger log = LoggerFactory.getLogger(WeatherTest.class);

	@Rule
	public Timeout globalTimeout= new Timeout(4500);

	@Test
	public void testWeatherStream() {

		try {
			URL url = WeatherTest.class.getResource("/remote-weather.xml");
			stream.run.main(url);
		} catch (Exception e) {
			fail("Could not run the ./remote-waether.xml");
			e.printStackTrace();
		}
	}

	@Test(expected= InterruptedException.class)
	public void weatherIoTest() {

		try {
			SourceURL url = new SourceURL(
					WeatherTest.class.getResource("/weather.data"));
			WeatherStream stream = new WeatherStream(url);
			stream.init();

			Data item = stream.read();
			while (item != null) {
				log.info("item: {} (size: {})", item,
						((byte[]) item.get("data")).length);

				item = stream.read();
			}
		} catch (Exception e) {
			fail("could not read stuff from the weather stream");
			e.printStackTrace();
		}
	}
	
	@Test(expected= InterruptedException.class)
	public void testByteStream() {

		URL url = Weird8ByteChunkStream.class.getResource("/weather.data");
		Weird8ByteChunkStream stream;
		try {
			stream = new Weird8ByteChunkStream(
					url.openStream());
		

			ByteBuffer msg = ByteBuffer.allocate(255);
	
			int i = 0;
			int limit = 100000;
			int last = 0;
			int buf = stream.read();
			int len = 0;
			while (i++ < limit) {
				// log.info("Byte: {}",
				// WeatherStream.getHex(new byte[] { (byte) buf }, 1));
	
				if (last == 0xffffffff && buf == 0xffffffff) {
					log.debug("Found message header!");
					if (len > 0) {
						log.info("Last message: {}",
								WeatherStream.getHex(msg.array(), len));
					}
					msg = ByteBuffer.allocate(255);
					len = 0;
				}
	
				last = buf;
				buf = stream.read();
				if (buf != 0xffffffff) {
					msg.put((byte) buf);
					len++;
				}
			}
			stream.close();
		} catch (IOException e) {
			fail("Could not open stream for url" + url);
			e.printStackTrace();
		}
	}
}