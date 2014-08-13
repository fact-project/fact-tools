package fact.io.zfits;

import fact.io.zfits.FitsHeader.ValueType;
import fact.io.zfits.ZFitsUtil.DataType;
import stream.util.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZFitsTable {
	private Map<String, FitsTableColumn> id2ColumnMap = new HashMap<String, FitsTableColumn>();
	private List<FitsTableColumn> columns = new ArrayList<FitsTableColumn>();

	private boolean isCompressed = false;
	private FitsHeader header = null;
	private int numCols = 0;
	private int numRows = 0;
	private int bytesPerRow = 0;
	private String tableName = "";

	public String toString() {
		return null;
	}

	public ZFitsTable(FitsHeader header) throws ParseException {
		this.header = header;
		this.isCompressed = header.check("ZTABLE", ValueType.BOOLEAN, "T");
		
		header.checkThrow("XTENSION", ValueType.STRING, "BINTABLE");
		header.checkThrow("NAXIS",    ValueType.INT,    "2");
		header.checkThrow("BITPIX",   ValueType.INT,    "8");
		header.checkThrow("GCOUNT",   ValueType.INT,    "1");
		header.checkThrow("EXTNAME",  ValueType.STRING);
		header.checkThrow("NAXIS1",   ValueType.INT);
		header.checkThrow("NAXIS2",   ValueType.INT);
		header.checkThrow("TFIELDS",  ValueType.INT);
		
		if (this.isCompressed) {
			try {
				header.checkThrow("ZNAXIS1", ValueType.INT);
				header.checkThrow("ZNAXIS2", ValueType.INT);
				header.checkThrow("ZPCOUNT", ValueType.INT, "0");
			} catch (ParseException e) {
				throw new ParseException("Missing headerkeys for compressed table: "+e.getMessage());
			}
		} else {
			if (!header.check("PCOUNT", ValueType.INT)) {
				throw new ParseException("Header does not match an Uncommpressed Fits Table, missing or wrong value types");
			}
		}
		
		this.tableName     = header.getKeyValue("EXTNAME").trim();
		int fixedTableSize = Integer.parseInt(header.getKeyValue("THEAP", "0"));
		int totalBytes     = Integer.parseInt(header.getKeyValue("NAXIS1"))*Integer.parseInt(header.getKeyValue("NAXIS2"));
		if (totalBytes != fixedTableSize && this.isCompressed)
			throw new ParseException("The 'THEAP' is not equal NAXIS1*NAXIS2, "+fixedTableSize+"!="+totalBytes);
		this.bytesPerRow = this.isCompressed ? Integer.parseInt(header.getKeyValue("ZNAXIS1")) : Integer.parseInt(header.getKeyValue("NAXIS1"));
		this.numRows    = this.isCompressed ? Integer.parseInt(header.getKeyValue("ZNAXIS2")) : Integer.parseInt(header.getKeyValue("NAXIS2"));
		this.numCols    = Integer.parseInt(header.getKeyValue("TFIELDS"));
		//long datasum    = this.isCompressed ? Long.parseLong(header.getKeyValue("ZDATASUM", "-1")) : Long.parseLong(header.getKeyValue("DATASUM","-1"));
		
		String formName = this.isCompressed ? "ZFORM" : "TFORM";

		int offset = 0;
		for (long i=0; i<this.numCols; i++) {
			String strNum = Long.toString(i+1);
			
			header.checkThrow("TTYPE"+strNum,  ValueType.STRING);
			header.checkThrow(formName+strNum, ValueType.STRING);
			
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
			
			FitsTableColumn column = new FitsTableColumn(id, numEntries, type.getNumBytes(), type, unit, compression);
			
			this.id2ColumnMap.put(id, column);
			this.columns.add(column);
			offset += column.getColumnSize();
		}
		
		if (offset != bytesPerRow) {
			throw new ParseException("Computed Size of Rowsize missmatches given size: "+offset+"!="+bytesPerRow);
		}
	}
	
	public String getTableName() {
		return this.tableName;
	}
	
	public int getNumTiles() {
		return Integer.parseInt(this.header.getKeyValue("NAXIS2"));
	}
	
	public int getBytesPerRow() {
		return this.bytesPerRow;
	}

	public int getNumRows() {
		return this.numRows;
	}

	public int getNumCols() {
		return this.numCols;
	}

	public long getHeapDifferenz() {
		long diff = getHeapSize();
		diff -= Long.parseLong(this.header.getKeyValue("THEAP", "0"));
		return diff;
	}
	
	public long getHeapSize() {
		if (!this.isCompressed) {
			return this.numRows*this.bytesPerRow;
		}
		if (!this.header.check("ZHEAPPTR"))
			return Long.parseLong(this.header.getKeyValue("THEAP", "0"));
		return Long.parseLong(this.header.getKeyValue("ZHEAPPTR", "0"));
	}
	
	/**
	 * This should work i hope
	 * @return The gap that i can't explain after the heap
	 */
	public long getSpezialGap() {
		//return this.numCols*16;
		return 0;
	}

	public long getSpezialAreaSize() {
		return Long.parseLong(header.getKeyValue("PCOUNT", "0"));
	}

	public long getPaddingSize() {
		long size = 0;
		// get offset of special data area from start of main table            
		//size += this.getHeapSize();
		
        // and special data area size
        size += this.getSpezialAreaSize();
        
        // spezial gap from somewhere
        //size += getSpezialGap();

        // necessary to answer with padding %2880
        return 2880-(size%2880);
        //return ((size+2871)/2880)*2880 - size;
	}

	public long getTableTotalSize() {
		long size = 0;
		// get size of fixed table data area          
		size += this.getHeapSize();
		
        // and heap data area size
        size += this.getSpezialAreaSize();
        
        // spezial gap from somewhere
        //size += this.getSpezialGap();

        // necessary to answer with padding %2880
        return ((size+2871)/2880)*2880;
	}
	
	public FitsTableColumn getColumns(int index) {
		return this.columns.get(index);
	}
	
	/**
	 * Return if the BINTABLE is a zfits table or not.
	 * @return True if zfits table.
	 */
	public boolean getCommpressed() {
		return this.isCompressed;
	}
	
	/**
	 * Returns the fits header which was used to create this table.
	 * @return The corresponding fits header.
	 */
	public FitsHeader getFitsHeader() {
		return this.header;
	}
}