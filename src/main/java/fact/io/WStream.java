/**
 * 
 */
package fact.io;

import java.io.InputStream;

import stream.io.SourceURL;

/**
 * @author chris
 * 
 */
public class WStream extends ByteChunkStream {

	public final static byte[] SIG = new byte[] { (byte) 0xff, (byte) 0xff };

	public WStream(SourceURL url) throws Exception {
		this(url.openStream());
	}

	/**
	 * @param in
	 * @param signature
	 * @throws Exception
	 */
	public WStream(InputStream in) throws Exception {
		super(new Weird8ByteChunkStream(in), SIG);
	}
}
