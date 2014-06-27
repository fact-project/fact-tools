/**
 * 
 */
package fact.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author chris
 * 
 */
public class Weird8ByteChunkStream extends InputStream {

	static Logger log = LoggerFactory.getLogger(Weird8ByteChunkStream.class);
	final InputStream in;
	byte[] buf = new byte[8];
	int avail = 0;
	int pos = 0;
	int position = 0;

	public Weird8ByteChunkStream(InputStream in) {
		this.in = in;
	}

	/**
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {

		//
		// serve data from the local buffer...
		//
		if (avail > 0) {
			avail--;
			// log.info("Returning byte at offset {}", position);
			position++;
			// log.info("Byte is: {}", (int) (0xffffffff & buf[pos]));
			return 0x0 | buf[pos++];
		}

		//
		// Fill the local buffer with a new 8-byte data message...
		//
		byte[] chunk = new byte[8];
		while (in.available() < 8) {
			// log.info("Waiting for new data...");
			sleep(500);
		}

		int read = in.read(chunk);
		while (read == 0) {
			read = in.read(chunk);
		}
		// log.info("Read {} bytes", read);
		if (read < 0) {
			throw new EOFException("end-of-stream reached!");
			// return -1;
		}
		// log.info("Reading {} bytes from 8-byte chunk", chunk[0]);

		//
		// copy the chunk into the local buffer...
		//
		int size = chunk[0];
		for (int i = 0; i < size; i++) {
			buf[i] = chunk[i + 1];
		}
		pos = 0;
		avail = size;

		return read();
	}

	public void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
