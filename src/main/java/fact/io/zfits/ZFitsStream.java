package fact.io.zfits;


import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;
import stream.util.parser.ParseException;

public class ZFitsStream extends AbstractStream {
	public class ZFitsStreamException extends Exception {
		private static final long serialVersionUID = 9189559930716693147L;
		
		public ZFitsStreamException(String message) {
			super(message);
		}
	}

	static Logger log = LoggerFactory.getLogger(ZFitsStream.class);
	private int bufferSize = 2880;

	private DataInputStream dataStream;
	private Data headerItem = DataFactory.create();
	private String tableName = "Events";
	
	private FitsTable fitsTable = null;
	private Catalog   catalog   = null;
	
	private TileHeader currentTileHeader = null;
	private int currentRowInTile = 0;
	private int currentTile = 0;

	@Override
	public Data readNext() throws Exception {
		Data item = DataFactory.create(headerItem);
		
		//Read the next TileHeader if necessary
		if (this.currentTile==this.fitsTable.getNumTiles()) {
			//we finished reading all data
			return null;
		}
		if (currentTileHeader==null) {
			byte[] data = new byte[TileHeader.getTileHeaderSize()];
			this.dataStream.read(data);
			this.currentTileHeader = new TileHeader(data);
			this.currentRowInTile  = 0;
		}
		
		//read the desired columns
		for (int colIndex=0; colIndex<this.fitsTable.getNumCols(); colIndex++) {
			//log.info("Read column number {}", colIndex);
			byte[] data = new byte[(int)catalog.getColumnSize(currentTile, colIndex)];
			if (data.length==0) {
				//ignore the column nothing in here
				continue;
			}
			this.dataStream.read(data);
			BlockHeader blockHeader = new BlockHeader(data);
			//get the decoded data
			data = blockHeader.decode();
			//insert it into the item with the correct format
			FitsTable.FitsTableColumn columnInfo = this.fitsTable.getColumns(colIndex);
			ByteBuffer buffer = null;
			switch (columnInfo.getType()) {
			case BOOLEAN:
				if (columnInfo.getNumEntries()==1) {
					boolean b = data[0]!=0;
					item.put(columnInfo.getId(), b);
				} else {
					boolean[] bArray = new boolean[columnInfo.getNumEntries()];
					for (int i=0; i<bArray.length; i++)
						bArray[i] = data[i]!=0;
					item.put(columnInfo.getId(), bArray);
				}
				break;
			case BYTE:
				if (columnInfo.getNumEntries()==1) {
					byte b = data[0];
					item.put(columnInfo.getId(), b);
				} else {
					byte[] bArray = new byte[columnInfo.getNumEntries()];
					for (int i=0; i<bArray.length; i++)
						bArray[i] = data[i];
					item.put(columnInfo.getId(), bArray);
				}
				break;
			case SHORT:
				buffer = ByteUtil.wrap(data);
				if (columnInfo.getNumEntries()==1) {
					short b = buffer.getShort();
					item.put(columnInfo.getId(), b);
				} else {
					short[] shortArray = new short[columnInfo.getNumEntries()];
					for (int i=0; i<shortArray.length; i++)
						shortArray[i] = buffer.getShort();
					item.put(columnInfo.getId(), shortArray);
				}
				break;
			case INT:
				buffer = ByteUtil.wrap(data);
				if (columnInfo.getNumEntries()==1) {
					int b = buffer.getInt();
					item.put(columnInfo.getId(), b);
				} else {
					int[] intArray = new int[columnInfo.getNumEntries()];
					for (int i=0; i<intArray.length; i++)
						intArray[i] = buffer.getInt();
					item.put(columnInfo.getId(), intArray);
				}
				break;
			case LONG:
				buffer = ByteUtil.wrap(data);
				if (columnInfo.getNumEntries()==1) {
					long b = buffer.getLong();
					item.put(columnInfo.getId(), b);
				} else {
					long[] longArray = new long[columnInfo.getNumEntries()];
					for (int i=0; i<longArray.length; i++)
						longArray[i] = buffer.getLong();
					item.put(columnInfo.getId(), longArray);
				}
				break;
			case DOUBLE:
				buffer = ByteUtil.wrap(data);
				if (columnInfo.getNumEntries()==1) {
					double b = buffer.getDouble();
					item.put(columnInfo.getId(), b);
				} else {
					double[] doubleArray = new double[columnInfo.getNumEntries()];
					for (int i=0; i<doubleArray.length; i++)
						doubleArray[i] = buffer.getDouble();
					item.put(columnInfo.getId(), doubleArray);
				}
				break;
			case FLOAT:
				buffer = ByteUtil.wrap(data);
				if (columnInfo.getNumEntries()==1) {
					float b = buffer.getFloat();
					item.put(columnInfo.getId(), b);
				} else {
					float[] doubleArray = new float[columnInfo.getNumEntries()];
					for (int i=0; i<doubleArray.length; i++)
						doubleArray[i] = buffer.getFloat();
					item.put(columnInfo.getId(), doubleArray);
				}
				break;
			case STRING:
				break;
			default:
				throw new ParseException("The type of a column is wrong, or could not be read.");
			}
		}
		//check if the current tile is finished
		this.currentRowInTile++;
		if (this.currentRowInTile == this.currentTileHeader.getNumRows()) {
			this.currentTileHeader = null;
			this.currentTile++;
		}
		log.debug(item.toString());
		return item;
	}

	public ZFitsStream(SourceURL url) throws Exception {
		super(url);
	}
	
	public ZFitsStream() {
		super();
	}

	@Override
	public void init() throws Exception {
		super.init();
		File f = new File(this.url.getFile());
		if (!f.canRead()){
			log.error("Cannot read file. Wrong path? ");
			throw new FileNotFoundException("Cannot read file");
		}
		this.dataStream = new DataInputStream(new BufferedInputStream(getInputStream(), bufferSize ));
		
		//FileInputStream fileInputStream = new FileInputStream(f);
		//this.dataStream = new DataInputStream(fileInputStream);
		// read first block and check if the first line starts with 'SIMPLE'
		// if not it is not a zfits file
		List<String> block = ZFitsFile.readBlock(this.dataStream);
		block.get(0).startsWith("SIMPLE");
		log.debug("Read First Header");
		//skip to the desired table
		log.info("Looking for table '{}'", this.tableName);
		while(true) {
			block = ZFitsFile.readBlock(this.dataStream);
			if (block==null)
				throw new NullPointerException("No table found or the given tableName is missing. Searching for: '"+this.tableName+"'");
			// read the header
			FitsHeader header = new FitsHeader(block);
			String extName = header.getKeyValue("EXTNAME");
			// read the Table
			this.fitsTable = new FitsTable(header);
			log.info("EXTNAME: {}", extName);
			if (!extName.trim().equals(this.tableName)) {
				// it is not the desired table so skip it entirely
				//log.info("Pos: 0x{}", Long.toHexString(fileInputStream.getChannel().position()));
				log.info("Skiping: {} bytes.",this.fitsTable.getTableTotalSize());
				long num = this.dataStream.skipBytes((int)this.fitsTable.getTableTotalSize());
				log.info("Skipped: {} bytes.", num);
				continue;
			}
			// create headerItem
			// add all key value pairs which are not the column information
			for (Map.Entry<String, FitsHeader.FitsHeaderEntry> entry : header.getKeyMap().entrySet()) {
				String key   = entry.getKey();
				String value = entry.getValue().getValue();
				switch(entry.getValue().getType()) {
				case BOOLEAN:
					if (value.equals("T"))
						this.headerItem.put(key, Boolean.TRUE);
					else
						this.headerItem.put(key, Boolean.FALSE);						
					break;
				case FLOAT:
					this.headerItem.put(key, Float.parseFloat(value));
					break;
				case INT:
					this.headerItem.put(key, Integer.parseInt(value));
					break;
				case STRING:
					this.headerItem.put(key, value);
					break;
				default:
					break;
				}
			}
			this.headerItem.put("@source", this.url.getProtocol() + ":" + this.url.getPath());
			
			// read the catalog
			byte[] fixTable = new byte[(int)this.fitsTable.getFixTableSize()];
			int numBytes = this.dataStream.read(fixTable);
			if (numBytes!=this.fitsTable.getFixTableSize())
				throw new ParseException("Could not read full heap");
			this.catalog = new Catalog(fixTable, this.fitsTable.getNumTiles(), this.fitsTable.getNumCols());

			break;
		}
	}
	
	public int getBufferSize() {
		return bufferSize;
	}

	@Parameter(required = false, description = "This value defines the size of the buffer of the BufferedInputStream", defaultValue = "8*1024")
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
	
	public String getTableName() {
		return this.tableName;
	}
	@Parameter(required = false, description = "This value defines which table of the ZFitsfile should be read.", defaultValue = "Events")
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
