package fact.io.zfits;

import java.nio.ByteBuffer;

public class TileHeader {
	public class FitsFileException extends Exception {
		private static final long serialVersionUID = 3249256194445950164L;
		public FitsFileException(String message) {
			super(message);
		}
		
	}
	private byte[] id = new byte[4]; // should be always 'TILE'
	private int    numRows; //number of rows pooled in this tile
	private long   size; //the size of the data following this tile (data of the tile)

	public TileHeader(byte[] input) throws FitsFileException {
		ByteBuffer buffer = ByteUtil.wrap(input);
		buffer.get(id);
		if (!new String(id).equals("TILE")) {
			throw new FitsFileException("Id of the TileHeader is wrong, got: '"+new String(id)+"'");
		}
		this.numRows = buffer.getInt(); 
		this.size    = buffer.getLong();
	}
	
	public static int getTileHeaderSize() {
		// 4(id)+4(numRows)+8(size)
		return 4+4+8;
	}
	
	public int getNumRows() {
		return this.numRows;
	}

	public String toString() {
		String s = "TILE, ID: '"+new String(id)+"', numRows: "+this.numRows+", size: "+this.size;
		return s;
	}
}