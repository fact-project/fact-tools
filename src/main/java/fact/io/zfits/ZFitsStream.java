package fact.io.zfits;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.annotations.Parameter;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;
import stream.util.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

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
	private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

	private ZFitsTable fitsTable = null;
	
	private TableReader tableReader = null;

	@Override
	public Data readNext() throws Exception {
		Data item = DataFactory.create(headerItem);
		
		if (this.tableReader == null)
			throw new NullPointerException("Didn't initialize the reader, should never happen.");
		//get the next row of data if zero we finished
		byte[][] dataRow = this.tableReader.readNextRow();
		if (dataRow == null)
			return null;

		//read the desired columns
		for (int colIndex=0; colIndex<this.fitsTable.getNumCols(); colIndex++) {
			byte[] data = dataRow[colIndex];

			//insert it into the item with the correct format
			FitsTableColumn columnInfo = this.fitsTable.getColumns(colIndex);
			ByteBuffer buffer = ZFitsUtil.wrap(data);
			buffer.order(this.byteOrder);
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
				String s = new String(buffer.array());
				item.put(columnInfo.getId(), s);
				break;
			default:
				throw new ParseException("The type of a column is wrong, or could not be read.");
			}
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
		log.info("Read file: {}", this.url.getFile());
		File f = new File(this.url.getFile());
		if (!f.canRead()){
			log.error("Cannot read file. Wrong path? ");
			throw new FileNotFoundException("Cannot read file");
		}
		this.dataStream = new DataInputStream(new BufferedInputStream(getInputStream(), bufferSize ));

		this.fitsTable = ZFitsUtil.skipToTable(this.dataStream, this.tableName);
		log.info("Found Table");
	
		// create headerItem
		// add all key value pairs which are not the column information TODO finish extracting column information
		for (Map.Entry<String, FitsHeader.FitsHeaderEntry> entry : this.fitsTable.getFitsHeader().getKeyMap().entrySet()) {
			String key   = entry.getKey();
			String value = entry.getValue().getValue();
			//ignore several information about the coloumns
			if (key.startsWith("TFORM") || key.startsWith("ZFORM") || key.startsWith("TTYPE") || key.startsWith("ZCTYP")
					|| key.startsWith("PCOUNT"))
				continue;
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
		//insert filename
		this.headerItem.put("@source", this.url.getProtocol() + ":" + this.url.getPath());
			
		//create the reader
		this.tableReader = BinTableReader.createTableReader(this.fitsTable, this.dataStream);

		if (this.fitsTable.getCommpressed()) {
			this.byteOrder = ByteOrder.LITTLE_ENDIAN;
		} else {
			this.byteOrder = ByteOrder.BIG_ENDIAN;
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
