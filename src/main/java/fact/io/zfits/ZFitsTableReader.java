package fact.io.zfits;

import fact.io.zfits.HuffmanCoder.DecodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.util.parser.ParseException;

import java.io.DataInputStream;
import java.io.IOException;

public class ZFitsTableReader implements TableReader {
	static Logger log = LoggerFactory.getLogger(ZFitsTableReader.class);

	private int currentRow = 0;
	private DataInputStream inputStream;
	private ZFitsTable table;
	private Catalog catalog;
	private int currentTile = 0;

	private TileHeader currentTileHeader = null;
	private int currentRowInTile = 0;
	private BlockHeader[] blockHeaders = null;

	/**
	 * 
	 * @param table
	 * @param inputStream
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public ZFitsTableReader(ZFitsTable table, DataInputStream inputStream) throws ParseException, IOException {
		this.table = table;
		this.inputStream = inputStream;
		//read Catalog
		// read the catalog
		byte[] fixTable = new byte[(int)this.table.getHeapSize()];
		int numBytes = this.inputStream.read(fixTable);
		if (numBytes!=this.table.getHeapSize())
			throw new ParseException("Could not read full heap");
		this.catalog = new Catalog(fixTable, this.table.getNumTiles(), this.table.getNumCols());
	}

	@Override
	public int skipRows(int num) throws IOException {
		return 0;
	}

	@Override
	public byte[][] readNextRow() throws IOException, ParseException, DecodingException {
		if (this.currentRow==table.getNumRows())
			return null;
		//Read the next TileHeader if necessary
		if (this.currentTile==this.table.getNumTiles()) {
			//we finished reading all data
			return null;
		}
		if (this.currentTileHeader==null) {
			log.debug("Reading tile: {}", this.currentTile);
			byte[] data = new byte[TileHeader.getTileHeaderSize()];
			this.inputStream.read(data);
			this.currentTileHeader = new TileHeader(data);
			//log.info("Num Rows: {}", this.currentTileHeader.getNumRows());
			this.currentRowInTile  = 0;
			
			//load data blocks
			this.blockHeaders = new BlockHeader[this.table.getNumCols()];
			for (int colIndex=0; colIndex<this.table.getNumCols(); colIndex++) {
				data = new byte[(int)catalog.getColumnSize(currentTile, colIndex)];
				if (data.length==0) { //ignore the column nothing in here
					continue;
				}
				this.inputStream.read(data);
				this.blockHeaders[colIndex] = new BlockHeader(data, this.currentTileHeader.getNumRows(), this.table.getColumns(colIndex));
				this.blockHeaders[colIndex].decode();
			}
		}

		byte[][] output = new byte[this.table.getNumRows()][];

		//log.info("Current Row in Tile: {}", this.currentRowInTile);
		//read the desired columns
		for (int colIndex=0; colIndex<this.table.getNumCols(); colIndex++) {
			//log.info("Load Col: "+colIndex);
			if (this.blockHeaders[colIndex]==null) {
				output[colIndex] = new byte[0];
				continue;
			}
			byte[] columnData = this.blockHeaders[colIndex].getDataRow(this.currentRowInTile);
			output[colIndex] = columnData;
		}
		//check if the current tile is finished
		this.currentRowInTile++;
		if (this.currentRowInTile == this.currentTileHeader.getNumRows()) {
			this.currentTileHeader = null;
			this.currentTile++;
		}

		return output;
	}
}
