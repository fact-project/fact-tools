package fact.io.zfits;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.util.parser.ParseException;

import java.io.DataInputStream;
import java.io.IOException;

public class BinTableReader implements TableReader {

    public static TableReader createTableReader(ZFitsTable table, DataInputStream input) throws ParseException, IOException {
		if (table.isCompressed) {
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

    /**
     * This method allocates a 2D byte array and fills it with the bytes from the columns as specified by the fits header.
     * It will return Null in case no more more bytes can be read from the file.
     * @return bytes from the file or NULL in case of EOF.
     * @throws IOException
     */
	@Override
	public byte[][] readNextRow() throws IOException {
		if (this.currentRow==table.getNumRows())
			return null;
		byte[][] output = new byte[this.table.getNumCols()][];
        int byteCounter = 0;
		for (int i=0; i<this.table.getNumCols(); i++) {
			output[i] = new byte[this.table.getColumns(i).getColumnSize()];
			int readbytes = this.inputStream.read(output[i]);
            byteCounter += readbytes;
            //check if EOF was reached
            if (readbytes == -1) {
                return null;
            }
        }
        return output;
	}
}
