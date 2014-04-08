package fact.io.zfits;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import stream.util.parser.ParseException;

/**
 * This class encapsulates the catalog describing the position and sizes of the table entries.
 * 
 * @author Michael Bulinski &lt;michael.bulinski@udo.edu&gt;
 */
public class Catalog {
	private int numTiles;
	private int numCols; 
	private long[][] offsetList;
	private long[][] sizeList;
	private long[] tileOffsetList;
	private long[] tileSizeList;

	/**
	 * Load the catalog of the position and sizes of the table entries.
	 * 
	 * @param input The catalog data to read.
	 * @param numTiles The number of tiles in the table.
	 * @param numCols The number of columns in the table.
	 * @throws ParseException Thrown when the catalog is broken.
	 */
	Catalog(byte[] input, int numTiles, int numCols) throws ParseException {
		this.numTiles = numTiles;
		this.numCols = numCols;
		ByteBuffer buffer = ByteUtil.wrap(input);
		buffer.order(ByteOrder.BIG_ENDIAN);
		
		int expectedCatalogSize = 2*numTiles*numCols*8;
		//if ( input.length !=  expectedCatalogSize)
		//	throw new ParseException("The catalog has : "+input.length+" bytes, this is to many or not enough entries. Expected: "+expectedCatalogSize+" bytes.");
		this.offsetList = new long[numTiles][numCols];
		this.sizeList   = new long[numTiles][numCols];
		this.tileOffsetList = new long[numTiles];
		this.tileSizeList   = new long[numTiles];

		for (int i=0; i<this.numTiles; i++) {
			for (int j=0; j<this.numCols; j++) {
				this.sizeList[i][j]  = buffer.getLong();
				this.offsetList[i][j]= buffer.getLong();
				//this.offsetList[i][j] -= this.offsetList[i][0];
				if (this.offsetList[i][j]<0 || this.sizeList[i][j]<0) {
					throw new ParseException("Catalog is broken, a value is negativ but should be positiv");
				}
				this.tileSizeList[i] += this.sizeList[i][j];
			}
			this.tileOffsetList[i] = this.offsetList[i][0]-TileHeader.getTileHeaderSize();
		}
	}
	
	public String toString() {
		String s= "Tiles: "+this.numTiles+", cols: "+this.numCols+"\n";
		for (int i=0; i<this.numTiles; i++) {
			for (int j=0; j<this.numCols; j++) {
				s += this.offsetList[i][j] + ":" + this.sizeList[i][j] + ','; 
			}
			s += '\n';
		}
		return s;
	}
	
	public long getTileOffset(int numTile) {
		return this.tileOffsetList[numTile];
	}
	
	public long getTileSize(int numTile) {
		return this.tileSizeList[numTile];
	}
	
	public long getColumnSize(int numTile, int numCol) {
		return this.sizeList[numTile][numCol];
	}
}