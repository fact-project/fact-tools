package fact.io.zfits;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.util.parser.ParseException;

import java.nio.ByteBuffer;

/**
 * Class representing a TileHeader.
 * @author Michael Bulinski
 */
public class TileHeader {
	static Logger log = LoggerFactory.getLogger(TileHeader.class);

	private byte[] id = new byte[4]; // should be always 'TILE'
	private int    numRows; //number of rows pooled in this tile
	private long   size; //the size of the data following this tile (data of the tile)

	/**
	 * Creates a TileHeader from the input byte array of a tile header.
	 * Format:
	 *     write id		 (byte[4])
	 *     write numRows (int 4 bytes)
	 *     write size    (long 8 bytes) Size of the data in bytes in the tile.
	 * @param input The byte-array containing the tile header.
	 * @throws stream.util.parser.ParseException
	 */
	public TileHeader(byte[] input) throws ParseException {
		ByteBuffer buffer = ZFitsUtil.wrap(input);
		buffer.get(id);
		String ids = new String(id);
		if (!ids.equals("TILE")) {
			log.info("Id of the TileHeader is wrong, got: '"+ids+"'");
			//should actually be the line below, but because of stuff we don't throw an error and ignore it
			//throw new ParseException("Id of the TileHeader is wrong, got: '"+ids+"'");
		}
		this.numRows = buffer.getInt(); 
		this.size    = buffer.getLong();
	}
	
	/**
	 * Returns the size of the tileHeader if written in a file.
	 * @return The size of the tileHeader.
	 */
	public static int getTileHeaderSize() {
		// 4(id)+4(numRows)+8(size)
		return 4+4+8;
	}
	
	/**
	 * Returns the number of Rows contained in the tile.
	 * @return The number of Rows contained in the tile.
	 */
	public int getNumRows() {
		return this.numRows;
	}

	public String toString() {
		String s = "TILE, ID: '"+new String(id)+"', numRows: "+this.numRows+", size: "+this.size;
		return s;
	}
}