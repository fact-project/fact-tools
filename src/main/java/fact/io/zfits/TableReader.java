package fact.io.zfits;

/**
 * Interface to a TableReader.
 * @author Michael Bulinski
 */
public interface TableReader {
	/**
	 * Skip the next row in the data. 
	 * @param num The number of Rows to skip.
	 * @return The number of rows skipped.
	 * @throws Exception Thrown if something went wrong.
	 */
	public int skipRows(int num) throws Exception;

	/**
	 * Get the next row in the table, returns null if there are no more rows in the table
	 * @return The row given as an array of the bytes of the columns.
	 * @throws Exception Thrown if something went wrong.
	 */
	public byte[][] readNextRow()  throws Exception;
}
