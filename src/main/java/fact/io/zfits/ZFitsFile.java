package fact.io.zfits;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import stream.util.parser.ParseException;
import fact.io.zfits.HuffmanCoder.DecodingException;

public class ZFitsFile {
	private DataInputStream input = null;

	//TODO move to Test
	public ZFitsFile(String filename) throws IOException, ParseException {
		FileInputStream fileInputStream = new FileInputStream(filename);
		this.input = new DataInputStream(fileInputStream);
		
		// read first block and check if the first line starts with 'SIMPLE'
		// if not it is not a zfits file
		List<String> block = ZFitsUtil.readBlock(this.input);
		block.get(0).startsWith("SIMPLE");
		
		//int numTable = 0;
		while(true) {
			block = ZFitsUtil.readBlock(this.input);
			if (block==null) {
				System.out.println("END");
				break;
			}
			if (block.get(0).startsWith("XTENSION")) {
				FitsHeader header = new FitsHeader(block);
				System.out.println("XTENSION header: \n"+header.toString());
				ZFitsTable table = new ZFitsTable(header);
				System.out.println("TotalSize: "+table.getTableTotalSize());
				/*if (numTable==0) {
					this.input.skip(table.getTableTotalSize());
					numTable++;
					continue;
				}*/
				//System.out.println("Start: "+Long.toHexString(fileInputStream.getChannel().position()));
				if (table.getCommpressed()) {
					//read catalog
					byte[] fixTable = new byte[(int)table.getHeapSize()];
					System.out.println("FixTableSize: "+table.getHeapSize());
					int numBytes = this.input.read(fixTable);
					if (numBytes!=table.getHeapSize())
						throw new ParseException("Could not read full heap");

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
							//System.out.println("Block Start: "+Long.toHexString(fileInputStream.getChannel().position()));
							input.read(data);
							BlockHeader blockHeader = new BlockHeader(data, tileHeader.getNumRows(), table.getColumns(j));
							System.out.println(blockHeader.toString());
							//FileOutputStream fos = new FileOutputStream(new File("/home/tarrox/physik/fact/byte.dat"));
							//BufferedOutputStream bos = new BufferedOutputStream(fos);
							//bos.write(blockHeader.data);
							byte[] output = null;
							try {
								 output = blockHeader.decode();
							} catch (DecodingException e) {
								throw new ParseException(e.getMessage());
							}
						}
						//this.input.skip(catalog.getTileSize(i));
					}
					//System.out.println("Data Count: "+table.getHeapSize());
					//System.out.println("Data End: "+Long.toHexString(fileInputStream.getChannel().position()));
					System.out.println("Position: "+Long.toHexString(fileInputStream.getChannel().position()));
					System.out.println("Padding: "+table.getPaddingSize());
					//System.out.println("TotalSize: "+table.getTableTotalSize());
					//System.out.println("Sum: "+(table.getFixTableSize()+table.getHeapSize()));
					long skipped = this.input.skip(table.getPaddingSize());
					//System.out.println("Skipped: "+skipped);
					//System.out.println("Pading End: "+Long.toHexString(fileInputStream.getChannel().position()));
					this.input.skip(table.getSpezialGap());
				} else {
					System.out.println("Normal Table");
					this.input.skip(table.getTableTotalSize());
				}
			} else {
				System.out.println(block.size());
				for (String entry : block) {
					System.out.println("'"+entry+"'");
				}
				throw new ParseException("Unknown block: '"+block.get(0)+"'");
			}
		}
	}
}

