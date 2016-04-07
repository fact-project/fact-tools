package fact.io.zfits;

import fact.io.zfits.ZFitsUtil.DataType;

public class FitsTableColumn {
	private String id;
	private int numEntries;	// number of Entries 
	private int entrySize;	// size of the entry in bytes
	private DataType type;
	private String unit;
	private String compressionName;

	public FitsTableColumn(String id, int numEntries, int entrySize, DataType type, String unit, String comp) {
		this.id = id;
		this.numEntries = numEntries;
		this.entrySize = entrySize;
		this.type = type;
		this.unit = unit;
		this.compressionName = comp;
	}
	
	public String getUnit() {
		return this.unit;
	}

	public int getEntrySize() {
		return this.entrySize;
	}
	
	public int getColumnSize() {
		return this.numEntries*this.entrySize;
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
	
	public String getCompressionName() {
		return this.compressionName;
	}
}