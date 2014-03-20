package fact.io.zfits;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import stream.util.parser.ParseException;
import fact.io.zfits.TileHeader.FitsFileException;

public class ZFitsFile {
	private DataInputStream input = null;

	public static List<String> readBlock(DataInputStream input) throws IOException {
		List<String> block = new ArrayList<String>();
		byte[] byteData = new byte[80];
		boolean loadingBlocks = true;
		while ( loadingBlocks ) {
			for (int i=0; i<36; i++) {
				int readBytes = input.read(byteData);
				if (readBytes==-1) { //end of stream
					return null;
				} else if (readBytes!=80) {
					throw new IOException("Could not read 80 Bytes from the File, it seems to be corrupted");
				}
				String stringData = new String(byteData);
				
				// If we got an empty line or an END the Info Block ended
				if (stringData.startsWith("END") || stringData.startsWith("    ")) {
					loadingBlocks = false;
					continue;
				}
				block.add(stringData);
			}
		}
		return block;
	}
	
	enum DataType {
		NONE(' ', 0),
		CHAR(' ', 1),
		STRING('A', 0),
		BOOLEAN('L', 1),
		BYTE('B', 1),
		SHORT('I', 2),
		INT('J', 4),
		LONG('K', 8),
		FLOAT('E', 4),
		DOUBLE('D', 8),
		COMPLEX('C', 8),
		BIT('X', 0),
		DOUBLE_COMPLEX('M', 16);
		
		
		// the character to use as description
		private final char character;
		private final int numBytes;
		private DataType(char character, int numBytes) {
			this.character = character;
			this.numBytes  = numBytes;
		}
		public char getCharacter() {
			return this.character;
		}
		public int getNumBytes() {
			return this.numBytes;
		}
		public static DataType getTypeFromChar(char ch) {
			for (DataType v : DataType.values()) {
				if (v.getCharacter() == ch) {
					return v;
				}
			}
			throw new NoSuchElementException("Charakter: '"+ch+"' does not corospond to a DataType");
		}
	}

	public class HeapArea {
		private Catalog catalog = null;
		private long dataAreaOffset = 0;
		
		public HeapArea(Catalog catalog, long dataAreaOffset) {
			this.catalog = catalog;
			this.dataAreaOffset = dataAreaOffset;
		}
		
		public void loadTileHeader(int numTile, DataInputStream input) throws IOException {
			long offset = this.catalog.getTileOffset(numTile);
			byte[] data = new byte[TileHeader.getTileHeaderSize()];
		}
	}

	//TODO move to Test
	public ZFitsFile(String filename) throws IOException, ParseException, FitsFileException {
		FileInputStream fileInputStream = new FileInputStream(filename);
		this.input = new DataInputStream(fileInputStream);
		
		// read first block and check if the first line starts with 'SIMPLE'
		// if not it is not a zfits file
		List<String> block = readBlock(this.input);
		block.get(0).startsWith("SIMPLE");
		
		//int numTable = 0;
		while(true) {
			block = readBlock(this.input);
			if (block==null) {
				System.out.println("END");
				break;
			}
			if (block.get(0).startsWith("XTENSION")) {
				FitsHeader header = new FitsHeader(block);
				System.out.println(header.toString());
				FitsTable table = new FitsTable(header);
				System.out.println(table.getTableTotalSize());
				/*if (numTable==0) {
					this.input.skip(table.getTableTotalSize());
					numTable++;
					continue;
				}*/
				//read catalog
				//System.out.println("Start: "+Long.toHexString(fileInputStream.getChannel().position()));
				byte[] fixTable = new byte[(int)table.getFixTableSize()];
				System.out.println("FixTableSize: "+table.getFixTableSize());
				int numBytes = this.input.read(fixTable);
				if (numBytes!=table.getFixTableSize())
					throw new ParseException("Could not read full heap");
				//skip not used bytes
				//this.input.skip(table.getHeapDifferenz());
				//System.out.println("Heap End: "+Long.toHexString(fileInputStream.getChannel().position()));
				Catalog catalog = new Catalog(fixTable, table.getNumTiles(), table.getNumCols());
				//System.out.println(catalog.toString());
				//load Tiles
				for (int i=0; i<table.getNumTiles(); i++) {
					byte[] data = new byte[TileHeader.getTileHeaderSize()];
					input.read(data);
					//System.out.println("Position: "+Long.toHexString(fileInputStream.getChannel().position()));
					TileHeader tileHeader = new TileHeader(data);
					System.out.print("Read Tile: "+i+" ");
					System.out.println(tileHeader.toString());
					//Load columns
					for (int j=0; j<table.getNumCols(); j++) {
						System.out.print("Read Col: "+j+"; ");
						if (catalog.getColumnSize(i, j)==0) {
							System.out.println("Column is empty");
							continue;
						}
						data = new byte[(int)catalog.getColumnSize(i, j)];
						input.read(data);
						BlockHeader blockHeader = new BlockHeader(data);
						System.out.println(blockHeader.toString());
					}
					//this.input.skip(catalog.getTileSize(i));
				}
				//System.out.println("Data Count: "+table.getHeapSize());
				//System.out.println("Data End: "+Long.toHexString(fileInputStream.getChannel().position()));
				//System.out.println("Position: "+Long.toHexString(fileInputStream.getChannel().position()));
				//System.out.println("Padding: "+table.getPaddingSize());
				//System.out.println("TotalSize: "+table.getTableTotalSize());
				//System.out.println("Sum: "+(table.getFixTableSize()+table.getHeapSize()));
				long skipped = this.input.skip(table.getPaddingSize());
				//System.out.println("Skipped: "+skipped);
				//System.out.println("Pading End: "+Long.toHexString(fileInputStream.getChannel().position()));
				this.input.skip(table.getSpezialGap());
			} else {
				throw new ParseException("Unknown block: '"+block.get(0)+"'");
			}
		}
	}
}
