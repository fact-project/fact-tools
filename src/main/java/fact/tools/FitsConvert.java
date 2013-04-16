/**
 * 
 */
package fact.tools;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.data.DataFactory;
import stream.io.CsvWriter;
import uk.ac.starlink.fits.FitsTableBuilder;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StoragePolicy;
import uk.ac.starlink.util.FileDataSource;

/**
 * @author chris
 * 
 */
public class FitsConvert {

	static Logger log = LoggerFactory.getLogger(FitsConvert.class);
	final static double JD_MJD_DIFF = 2400000.5d;
	final static double UNIX_JD_OFFSET = 24405867.5 * 86400;

	public static Long mjd2unixtime(Double mjd) {
		double jd = mjd + JD_MJD_DIFF;
		Double unix = jd - UNIX_JD_OFFSET;
		return unix.longValue();
	}

	public static void dumpArray(Object array, String baseName, Data item) {

		if (!array.getClass().isArray()) {
			log.error("Class {} is not an array!", array);
			return;
		}

		Class<?> valueType = array.getClass().getComponentType();
		log.debug("Array content is of type {}", valueType);

		if (valueType == float.class) {
			log.trace("Dumping float[] array...");
			float[] vec = (float[]) array;
			for (int i = 0; i < vec.length; i++) {
				String var = baseName + "_" + i;
				item.put(var, "" + vec[i]);
			}
			return;
		}

		if (valueType == int.class) {
			log.trace("Dumping int[] array...");
			int[] vec = (int[]) array;
			for (int i = 0; i < vec.length; i++) {
				String var = baseName + "_" + i;
				item.put(var, "" + vec[i]);
			}
			return;
		}

		if (valueType == short.class) {
			log.trace("Dumping short[] array...");
			short[] vec = (short[]) array;
			for (int i = 0; i < vec.length; i++) {
				String var = baseName + "_" + i;
				item.put(var, "" + vec[i]);
			}
			return;
		}

		if (valueType == String.class) {
			log.trace("Dumping string[] array...");
			String[] vec = (String[]) array;
			for (int i = 0; i < vec.length; i++) {
				String var = baseName + "_" + i;
				item.put(var, "" + vec[i]);
			}
			return;
		}

		if (valueType == double.class) {
			log.trace("Dumping double[] array...");
			double[] vec = (double[]) array;
			for (int i = 0; i < vec.length; i++) {
				String var = baseName + "_" + i;
				item.put(var, "" + vec[i]);
			}
			return;
		}

		if (!"".equals(baseName.trim())) {
			log.error("Don't know how to dump array of {}'s (column-name: {})",
					valueType, baseName);
		}
	}

	public static void dumpFitsToCSV(File fitsFile, File outputFile)
			throws Exception {

		log.info("Dumping file {} to {}", fitsFile, outputFile);

		String name = fitsFile.getName();
		log.info("Fits filename is {}", name);

		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");

		int idx = name.indexOf("_");
		int end = name.indexOf(".", idx);
		Date date = fmt.parse(name.substring(0, idx));
		log.info("Date should be {}", date);
		log.info("Run-number should be {}", name.subSequence(idx + 1, end));

		Integer runNumber = new Integer(name.substring(idx + 1, end));

		FileDataSource ds = new FileDataSource(fitsFile);
		CsvWriter dsw = new CsvWriter(outputFile);

		FitsTableBuilder ftb = new FitsTableBuilder();
		StarTable table = ftb.makeStarTable(ds, false, StoragePolicy.ADAPTIVE);
		ArrayList<String> cols = new ArrayList<String>();

		for (int c = 0; c < table.getColumnCount(); c++) {
			ColumnInfo col = table.getColumnInfo(c);
			log.info("Adding column '{}'", col.getName());
			cols.add(col.getName());
		}

		Long maxRows = Long.MAX_VALUE;
		try {
			maxRows = new Long(System.getProperty("max.rows"));
		} catch (Exception e) {
			maxRows = Long.MAX_VALUE;
		}

		if (System.getProperty("list-columns") != null) {
			return;
		}

		Long count = 0L;

		Data item = DataFactory.create();

		RowSequence rowSeq = table.getRowSequence();
		while (rowSeq.next() && count < maxRows) {
			item.clear();
			item.put("date", date.getTime());
			item.put("run", runNumber);
			item.put("file", name);
			Object[] row = rowSeq.getRow(); // table.getRow( rowId );
			for (int c = 0; c < row.length; c++) {
				String colName = cols.get(c);
				Object val = row[c];
				Class<?> clazz = val.getClass();
				if (clazz.isArray()) {

					dumpArray(val, colName, item);

				} else {
					item.put(colName, row[c] + "");
				}
			}
			dsw.process(item);
		}
		dsw.finish();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		List<File> files = new ArrayList<File>();

		if (args.length < 1) {
			System.err.println("Usage:\n");
			System.err
					.println("  java edu.udo.cs.ai.fact.tools.FitsConvert FITS_FILE\n ");
			return;
		}

		File input = new File(args[0]);

		if (input.isFile()) {
			files.add(input);
		} else {
			File[] flist = input.listFiles();
			if (flist != null) {
				for (File f : flist) {
					if (f.getName().toLowerCase().endsWith(".fits")) {
						files.add(f);
					}
				}
			}
		}

		log.info("Need to dump {} files: {}", files.size(), files);

		for (File currentFile : files) {
			File outputFile = new File(currentFile.getName().replaceAll(
					"\\.fits$", ".csv"));
			dumpFitsToCSV(currentFile, outputFile);
		}
	}
}

/*
 * FileDataSource ds = new FileDataSource( new File(
 * "20111120.FSC_CONTROL_TEMPERATURE.fits" ) ); DataStreamWriter dsw = new
 * DataStreamWriter( new File( "/Users/chris/FSC_CONTROL_TEMP.csv" ) );
 * 
 * 
 * FitsTableBuilder ftb = new FitsTableBuilder(); StarTable table =
 * ftb.makeStarTable( ds, false , StoragePolicy.PREFER_MEMORY );
 * ArrayList<String> cols = new ArrayList<String>();
 * 
 * for( int c = 0; c < table.getColumnCount(); c++ ){ ColumnInfo col =
 * table.getColumnInfo( c ); System.out.println( "Adding column '" +
 * col.getName() + "'" ); cols.add( col.getName() ); }
 * 
 * Data item = new DataImpl();
 * 
 * for( long rowId = 0; rowId < table.getRowCount(); rowId++ ){ item.clear();
 * Object[] row = table.getRow( rowId ); for( int c = 0; c < row.length; c++ ){
 * String colName = cols.get( c ); Object val = row[c]; if(
 * val.getClass().isArray() ){ //System.out.println( "array type: " +
 * val.getClass() ); float[] vec = (float[]) val;
 * 
 * //System.out.print( "{" ); for( int i = 0; i < vec.length; i++ ){ String var
 * = colName + "_" + i; //System.out.print( " " + var + ":" + vec[i] );
 * item.put( var, "" + vec[i] ); } //System.out.println( " }" ); } else {
 * //System.out.println( "  " + row[c] ); //System.out.println( cols.get(c) +
 * ": " + row[c] );
 * 
 * item.put( colName, row[c] + "" ); } } dsw.process( item ); } dsw.close();
 */
