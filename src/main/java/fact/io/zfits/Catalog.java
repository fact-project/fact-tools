package fact.io.zfits;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import stream.util.parser.ParseException;

public class Catalog {
	private int numTiles;
	private int numCols; 
	private long[][] offsetList;
	private long[][] sizeList;
	private long[] tileOffsetList;
	private long[] tileSizeList;

	Catalog(byte[] input, int numTiles, int numCols) throws ParseException {
		this.numTiles = numTiles;
		this.numCols = numCols;
		ByteBuffer buffer = ByteUtil.wrap(input);
		buffer.order(ByteOrder.BIG_ENDIAN);
		
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