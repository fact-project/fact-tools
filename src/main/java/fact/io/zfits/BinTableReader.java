package fact.io.zfits;

import java.io.DataInputStream;
import java.io.IOException;

import stream.util.parser.ParseException;

//TODO create as Interface
public class BinTableReader implements TableReader {
	public static TableReader createTableReader(ZFitsTable table, DataInputStream input) throws ParseException, IOException {
		if (table.getCommpressed()) {
			return new ZFitsTableReader(table, input);
		} else {
			//normale Bintable
			return new BinTableReader(table, input);
		}
	}
	
	private ZFitsTable table;
	private DataInputStream inputStream;
	private int currentRow = 0;

	public BinTableReader(ZFitsTable table, DataInputStream inputStream) {
		this.table = table;
		this.inputStream = inputStream;
	}

	@Override
	public int skipRows(int num) throws IOException {
		if (this.currentRow==table.getNumRows())
			return 0;
		if (this.currentRow+num>table.getNumRows())
			num = table.getNumRows()-this.currentRow;
		inputStream.skipBytes(num*table.getBytesPerRow());
		this.currentRow += num;
		return num;
	}

	@Override
	public byte[][] readNextRow() throws IOException {
		if (this.currentRow==table.getNumRows())
			return null;
		byte[][] output = new byte[this.table.getNumRows()][];
		for (int i=0; i<this.table.getNumCols(); i++) {
			output[i] = new byte[this.table.getColumns(i).getColumnSize()];
			this.inputStream.read(output[i]);
		}
		return output;
	}
}
