package fact.io.zfits;

import java.nio.ByteBuffer;

import stream.util.parser.ParseException;

/**
 * Class representing a TileHeader.
 * @author Michael Bulinski
 */
public class TileHeader {
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
	 * @throws FitsFileException
	 */
	public TileHeader(byte[] input) throws ParseException {
		ByteBuffer buffer = ZFitsUtil.wrap(input);
		buffer.get(id);
		if (!new String(id).equals("TILE")) {
			throw new ParseException("Id of the TileHeader is wrong, got: '"+new String(id)+"'");
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