/**
 * 
 */
package fact.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;
import stream.util.ByteSize;

/**
 * <p>
 * This class implements a fast byte-oriented stream of byte chunks. The chunks
 * are found by checking for a start-signature (i.e. byte array). The stream
 * returns a sequence of data items, each holding a chunk of bytes.
 * </p>
 * 
 * @author Christian Bockermann &lt;chris@jwall.org&gt;
 * 
 */
public abstract class ByteChunkStream extends AbstractStream {

	static Logger log = LoggerFactory.getLogger(ByteChunkStream.class);
	public final static byte[] GIF_SIGNATURE = new byte[] { 0x47, 0x49, 0x46,
			0x38 };
	public final static byte[] JPG_SIGNATURE = new byte[] { (byte) 0xff,
			(byte) 0xd8 };

	// by default, we buffer only 2 MB of data from the input stream...
	public final static int DEFAULT_BUFFER_SIZE = 32 * 1024;

	SourceURL url;
	ByteBuffer buffer;

	InputStream input;

	/* The byte-channel which is being read from using NIO */
	final ReadableByteChannel channel;

	Long frameId = 0L;
	final byte[] signature;

	/* The name of the attribute into which the byte arrays (chunks) are put */
	String key = "data";

	// The 'look-ahead' buffer
	int bufferSize = 2 * 1024 * 1024;

	// the timestamp of the first read (for debugging/timing-measurement)
	Long firstRead = 0L;

	// the number of bytes read so fare
	Long bytesRead = 0L;
	Long chunks = 0L;

	public ByteChunkStream(SourceURL url, byte[] signature) throws Exception {
		this(url.openStream(), signature);
		this.url = url;
	}

	public ByteChunkStream(InputStream in, byte[] signature) throws Exception {
		// this.input = new BufferedInputStream(in);
		input = in;
		channel = Channels.newChannel(in);
		this.signature = signature;
	}

	/**
	 * @see stream.io.AbstractDataStream#init()
	 */
	@Override
	public void init() throws Exception {
		super.init();
		log.info("Using buffer size of {}k", this.bufferSize / 1024);
		buffer = ByteBuffer.allocateDirect(bufferSize);
		if (buffer.isDirect()) {
			log.info("ByteBuffer is using direct memory.");
		} else {
			log.info("ByteBuffer is non-direct memory.");
		}
	}

	/**
	 * @see stream.io.DataStream#close()
	 */
	@Override
	public void close() throws Exception {
		super.close();
		buffer.clear();
		channel.close();
	}

	/**
	 * @return the bufferSize
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * @param bufferSize
	 *            the bufferSize to set
	 */
	@Parameter(description = "The internal buffer size of this stream.")
	public void setBufferSize(ByteSize bufferSize) {
		this.bufferSize = bufferSize.getBytes();
	}

	/**
	 * 
	 * @param sig
	 * @return
	 */
	private int indexOf(byte[] sig) {
		return indexOf(sig, 0);
	}

	/**
	 * 
	 * @param sig
	 * @param from
	 * @return
	 */
	protected int indexOf(byte[] sig, int from) {
		int pos = from;

		while (pos + sig.length < buffer.limit() && !isSignatureAt(pos, sig)) {
			pos++;
		}

		if (pos + sig.length >= buffer.limit()) {
			return -1;
		}

		return pos;
	}

	/**
	 * 
	 * 
	 * @param pos
	 * @param sig
	 * @return
	 */
	protected boolean isSignatureAt(int pos, byte[] sig) {

		for (int i = 0; i < sig.length; i++) {
			byte b = buffer.get(pos + i);
			if (b != sig[i]) {
				return false;
			}
		}

		return true;
	}

	private int readBytes() throws IOException {
		// int bytes = 0;
		//
		// int b = -1;
		// do {
		// b = input.read();
		// if (b >= 0) {
		// buffer.put((byte) b);
		// bytes++;
		// }
		// } while (b >= 0);
		// return bytes;
		return channel.read(buffer);
	}

	/**
	 * @see stream.io.AbstractDataStream#readItem(stream.data.Data)
	 */
	@Override
	public synchronized Data readNext() throws Exception {

		int read = readBytes();
		while (read == 0) {
			Thread.yield();
			read = readBytes();
			if (read < 0) {
				// log.debug("No more data to read...");
				return null;
			}
		}

		if (read > 0) {
			bytesRead += read;
			// log.debug("{} bytes read so far...", bytesRead);
		}

		int start = indexOf(signature);
		while (start < 0) {
			//
			// skip to the end of the buffer and clear it
			//
			// log.info("No start found, skipping to buffer end, compacting the buffer and reading new data...");
			buffer.position(buffer.limit() - signature.length);
			buffer.compact();

			// read new data into the buffer
			//
			read = readBytes();

			while (read == 0 && buffer.hasRemaining()) {
				Thread.yield();
				read = readBytes();
			}

			bytesRead += read;
			start = indexOf(signature);
			// log.debug("Found start: {}", start);

			if (start < 0 && read < 0) {
				return null;
			}
		}

		buffer.mark();

		int end = indexOf(signature, start + signature.length);
		while (end < 0 && buffer.capacity() > 0) {
			read = channel.read(buffer);

			while (read == 0) {
				Thread.yield();
				read = readBytes();
			}

			if (read < 0) {
				return null;
			}

			end = indexOf(signature);
			// log.debug("Found end: {}", end);
		}

		if (end < 0) {
			return null;
		}

		buffer.position(start);

		byte[] output = new byte[end - start];
		buffer.get(output, 0, (end - start));

		buffer.compact();
		Data instance = DataFactory.create();
		instance.put(key, output);
		chunks++;

		if (firstRead == 0L) {
			firstRead = System.currentTimeMillis();
		} else {
			if (chunks % 100 == 0) {
				Long seconds = (System.currentTimeMillis() - firstRead);
				log.debug("Reading rate after {} chunks is {} chunks/second",
						chunks,
						(1000 * (chunks.doubleValue() / seconds.doubleValue())));
				log.debug("{} bytes read", bytesRead);
			}
		}

		return instance;
	}
}