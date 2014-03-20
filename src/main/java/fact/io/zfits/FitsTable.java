package fact.io.zfits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stream.util.parser.ParseException;
import fact.io.zfits.FitsHeader.ValueType;
import fact.io.zfits.ZFitsFile.DataType;

public class FitsTable {
	private Map<String, FitsTableColumn> id2ColumnMap = new HashMap<String, FitsTableColumn>();
	private List<FitsTableColumn> columns = new ArrayList<FitsTableColumn>();

	public class FitsTableColumn {
		private String id;
		private int offset;
		private int numEntries;	// number of Entries 
		private int entrySize;	// size of the entry in bytes
		private int columnSize; // num*entrySize
		private DataType type;
		private String unit;
		private String compressionName;
		
		public FitsTableColumn(String id, int offset, int numEntries, int entrySize, DataType type, String unit, String comp) {
			this.id = id;
			this.offset = offset;
			this.numEntries = numEntries;
			this.entrySize = entrySize;
			this.columnSize = numEntries*entrySize;
			this.type = type;
			this.unit = unit;
			this.compressionName = comp;
		}
		
		public DataType getType() {
			return this.type;
		}
		
		public int getNumEntries() {
			return this.numEntries;
		}
		
		public String getId() {
			return this.id;
		}
	}

	private boolean isCompressed = false;
	private FitsHeader header = null;
	private int numCols = 0;

	public String toString() {
		return null;
	}

	public FitsTable(FitsHeader header) throws ParseException {
		this.header = header;
		this.isCompressed = header.check("ZTABLE", ValueType.BOOLEAN, "T");
		
		if (!header.check("XTENSION", ValueType.STRING, "BINTABLE"))
			throw new ParseException("Header does not match an Fits Table, missing or wrong value types");
		if (!header.check("NAXIS",    ValueType.INT,    "2"))
			throw new ParseException("Header does not match an Fits Table, missing or wrong value types");
		if (!header.check("BITPIX",   ValueType.INT,    "8"))
			throw new ParseException("Header does not match an Fits Table, missing or wrong value types");
		if (!header.check("GCOUNT",   ValueType.INT,    "1"))
			throw new ParseException("Header does not match an Fits Table, missing or wrong value types");
		if (!header.check("EXTNAME",  ValueType.STRING))
			throw new ParseException("Header does not match an Fits Table, missing or wrong value types");
		if (!header.check("NAXIS1",   ValueType.INT))
			throw new ParseException("Header does not match an Fits Table, missing or wrong value types");
		if (!header.check("NAXIS2",   ValueType.INT))
			throw new ParseException("Header does not match an Fits Table, missing or wrong value types");
		if (!header.check("TFIELDS",  ValueType.INT))
			throw new ParseException("Header does not match an Fits Table, missing or wrong value types");
		
		if (this.isCompressed) {
			if (!header.check("ZNAXIS1", ValueType.INT) ||
				!header.check("ZNAXIS2", ValueType.INT) ||
				!header.check("ZPCOUNT", ValueType.INT, "0")) {
				throw new ParseException("Header does not match an Commpressed Fits Table, missing or wrong value types");
			}
		} else {
			if (!header.check("PCOUNT", ValueType.INT)) {
				throw new ParseException("Header does not match an Uncommpressed Fits Table, missing or wrong value types");
			}
		}
		
		String name     = header.getKeyValue("EXTNAME");
		int fixedTableSize = Integer.parseInt(header.getKeyValue("THEAP", "0"));
		int totalBytes = Integer.parseInt(header.getKeyValue("NAXIS1"))*Integer.parseInt(header.getKeyValue("NAXIS2"));
		if (totalBytes != fixedTableSize)
			throw new ParseException("The 'THEAP' is not equal NAXIS1*NAXIS2");
		int bytesPerRow = this.isCompressed ? Integer.parseInt(header.getKeyValue("ZNAXIS1")) : Integer.parseInt(header.getKeyValue("NAXIS1"));
		int numRows     = this.isCompressed ? Integer.parseInt(header.getKeyValue("ZNAXIS2")) : Integer.parseInt(header.getKeyValue("NAXIS2"));
		this.numCols     = Integer.parseInt(header.getKeyValue("TFIELDS"));
		long datasum    = this.isCompressed ? Long.parseLong(header.getKeyValue("ZDATASUM", "-1")) : Long.parseLong(header.getKeyValue("DATASUM","-1"));
		
		int bytes = 0;
		
		String formName = this.isCompressed ? "ZFORM" : "TFORM";

		int offset = 0;
		for (long i=0; i<this.numCols; i++) {
			String strNum = Long.toString(i+1);
			
			if (!header.check("TTYPE"+strNum,  ValueType.STRING))
				throw new ParseException("Missing row description 'TTYPE"+strNum+"' or row description is wrong type");
			if (!header.check(formName+strNum, ValueType.STRING))
				throw new ParseException("Missing row description '"+formName+"' or row description is wrong type");
			
			String id = header.getKeyValue("TTYPE"+strNum);
			String unit = header.getKeyValue("TUNIT"+strNum, "");
			
			String compression = header.getKeyValue("ZCTYP"+strNum, "");
			if (isCompressed) {
				if (!compression.equals("FACT") && !compression.isEmpty())
					throw new ParseException("Only Fact compression supported, but for row: '"+strNum+"' we got: "+compression);
			}
			
			
			String format = header.getKeyValue(formName+strNum);
			Integer tmp = Integer.parseInt(format.substring(0, format.length()-1));
			if (tmp==null)
				throw new ParseException("Can't get the Format from row: "+strNum+" format is: "+format);
			int numEntries = tmp.intValue();
			DataType type = DataType.getTypeFromChar(format.charAt(format.length()-1));
			
			FitsTableColumn column = new FitsTableColumn(id, offset, numEntries, type.getNumBytes(), type, unit, compression);
			
			this.id2ColumnMap.put(id, column);
			this.columns.add(column);
			offset += numEntries*type.getNumBytes();
		}
		
		if (offset != bytesPerRow) {
			throw new ParseException("Computed Size of Rowsize missmatches given size: "+offset+"!="+bytesPerRow);
		}
	}
	
	public int getNumTiles() {
		return Integer.parseInt(this.header.getKeyValue("NAXIS2"));
	}
	public int getNumCols() {
		return this.numCols;
	}

	public long getHeapDifferenz() {
		long diff = getFixTableSize();
		diff -= Long.parseLong(this.header.getKeyValue("THEAP", "0"));
		return diff;
	}
	
	public long getFixTableSize() {
		return Long.parseLong(this.header.getKeyValue("ZHEAPPTR", "0"));
		//return Long.parseLong(this.header.getKeyValue("THEAP", "0"));
	}
	
	/**
	 * This should work i hope
	 * @return The gap that i can't explain after the heap
	 */
	public long getSpezialGap() {
		return this.numCols*16;
	}

	public long getHeapSize() {
		return Long.parseLong(header.getKeyValue("PCOUNT", "0"));
	}

	public long getPaddingSize() {
		long size = 0;
		// get offset of special data area from start of main table            
		size += this.getFixTableSize();
		
        // and special data area size
        size += this.getHeapSize();
        
        // spezial gap from somewhere
        //size += getSpezialGap();

        // necessary to answer with padding %2880
        return 2880-(size%2880);
        //return ((size+2871)/2880)*2880 - size;
	}

	public long getTableTotalSize() {
		long size = 0;
		// get size of fixed table data area          
		size += this.getFixTableSize();
		
        // and heap data area size
        size += this.getHeapSize();
        
        // spezial gap from somewhere
        size += getSpezialGap();

        // necessary to answer with padding %2880
        return ((size+2871)/2880)*2880;
	}
	
	public FitsTableColumn getColumns(int index) {
		return this.columns.get(index);
	}
}