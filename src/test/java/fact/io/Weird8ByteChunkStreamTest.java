/**
 * 
 */
package fact.io;

import java.net.URL;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chris
 * 
 */
public class Weird8ByteChunkStreamTest {

	static Logger log = LoggerFactory
			.getLogger(Weird8ByteChunkStreamTest.class);

	public static void main(String args[]) throws Exception {

		URL url = Weird8ByteChunkStream.class.getResource("/weather.data");
		Weird8ByteChunkStream stream = new Weird8ByteChunkStream(
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
	}
}
