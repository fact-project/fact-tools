package fact.io.zfits;

import org.apache.commons.cli.MissingArgumentException;
import org.jfree.util.Log;

import stream.util.parser.ParseException;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A class containing several different util function for ZFitsFile reading. 
 * @author Michael Bulinski
 *
 */
public class ZFitsUtil {
	/**
	 * Enumeration containing the diffrent types of datatypes supported by the fits format.
	 * @author Michael Bulinski
	 */
	public static enum DataType {
		NONE(' ', 0),
		STRING('A', 1),
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

		/**
		 * Parses the type and returns the enum of it. If it isn't a supported type it throw an Exception.
		 * @param ch The type to parse.
		 * @return The enum describing the type.
		 */
		public static DataType getTypeFromChar(char ch) {
			for (DataType v : DataType.values()) {
				if (v.getCharacter() == ch) {
					return v;
				}
			}
			throw new NoSuchElementException("Charakter: '"+ch+"' does not corospond to a DataType");
		}
	}

	/**
	 * Reads all blocks off one fits header and returns them as an array of 80 character wide Strings. 
	 * @param input An inputStream pointing to the start of a fits header block. 
	 * @return See description.
	 * @throws IOException Thrown if something went wrong reading from input.
	 */
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
	
	/**
	 * Reads the headers until the BINTABLE header with the name tableName is found.
	 *  
	 * @param input The InputStream pointing to the first header.
	 * @param tableName The tablename to look for
	 * @return The parsed ZFitsTable from the found header.
	 * 
	 * @throws ParseException
	 * @throws IOException
	 * @throws MissingArgumentException 
	 */
	public static ZFitsTable skipToTable(DataInputStream input, String tableName) throws ParseException, IOException, MissingArgumentException {
		ZFitsTable fitsTable = null;
		//System.out.println("Searching for: "+tableName);
		while(true) {
			List<String> block = ZFitsUtil.readBlock(input);
			if (block==null)
				throw new NullPointerException("No table found or the given tableName is missing. Searching for: '"+tableName+"'");
			if (!block.get(0).startsWith("XTENSION")) {
				continue;
			}
			// read the header
			FitsHeader header = new FitsHeader(block);

			//read the table
			fitsTable = new ZFitsTable(header);

//			System.out.println("Found table: "+fitsTable.getTableName());
			if (!fitsTable.getTableName().equals(tableName)) {
				// it is not the desired table so skip it entirely
//				System.out.println("Skipping: "+fitsTable.getTableTotalSize());
				long num = input.skipBytes((int) fitsTable.getTableTotalSize());
//				long num = input.skip(fitsTable.getTableTotalSize());
//				System.out.println("Num: "+num);
				if (num!=(int)fitsTable.getTableTotalSize())
					throw new MissingArgumentException("Couldn't skip the table, maybe file is corrupted or table is missing. Name: "+tableName);
				continue;
			}
			return fitsTable;
		}
	}

	public static ByteBuffer wrapBig(byte[] data) {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.BIG_ENDIAN);
		return buffer;
	}
	public static ByteBuffer wrap(byte[] data) {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer;
	}
	
	public static ByteBuffer create(int size) {
		byte[] data = new byte[size];
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer;
	}

	public static ByteBuffer create(long size) {
		byte[] data = new byte[(int)size];
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer;
	}
	
	public static void printByteArray(byte[] array) {
		for (int i=0; i<array.length; i++) {
			System.out.println(String.format("%8s", Integer.toBinaryString(array[i]&0xFF)).replace(' ', '0'));
		}
	}
}
