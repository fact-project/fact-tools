/**
 * 
 */
package fact.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.data.DataFactory;
import stream.io.AbstractStream;
import stream.io.SourceURL;
import uk.ac.starlink.fits.FitsTableBuilder;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StoragePolicy;
import uk.ac.starlink.util.DataSource;
import uk.ac.starlink.util.FileDataSource;
import uk.ac.starlink.util.URLDataSource;

/**
 * @author chris
 * 
 */
public class FitsDataStream extends AbstractStream {

	static Logger log = LoggerFactory.getLogger(FitsDataStream.class);
	Map<String, Class<?>> header;
	File fitsFile;
	// List<Processor> preProcessors = new ArrayList<Processor>();
	RowSequence rowSequence;
	List<String> cols = new ArrayList<String>();

	Long limit = -1L;
	String id = null;
	final DataSource ds;

	/**
	 * @param url
	 * @throws Exception
	 */
	public FitsDataStream(File file) throws Exception {
		this(new FileDataSource(file));
	}

	public FitsDataStream(URL url) throws Exception {
		this(new URLDataSource(url));
	}

	public FitsDataStream(SourceURL sUrl) throws Exception {
		// URL nUrl = new URL(sUrl.toString());
		this(new URLDataSource(new URL(sUrl.getProtocol(), sUrl.getHost(),
				sUrl.getFile())));
	}

	public FitsDataStream(DataSource ds) throws Exception {
		this.ds = ds;
		/*
		 * FitsTableBuilder ftb = new FitsTableBuilder(); StarTable table =
		 * ftb.makeStarTable(ds, false, StoragePolicy.ADAPTIVE);
		 * 
		 * header = checkHeader(table); rowSequence = table.getRowSequence();
		 * 
		 * for (int c = 0; c < table.getColumnCount(); c++) { ColumnInfo col =
		 * table.getColumnInfo(c); // log.info( "Adding column '{}'",
		 * col.getName() ); if (!col.getName().trim().equals("")) {
		 * cols.add(col.getName()); } }
		 */
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	/**
	 * @see stream.io.DataStream#init()
	 */
	@Override
	public void init() throws Exception {
		log.debug("Initializing stream...");
		FitsTableBuilder ftb = new FitsTableBuilder();
		File f = new File(ds.toString());
		if(f.canRead()){
			long rest = f.length()%2880;
			log.debug("File size " + f.length());
			log.debug("Filesize % 2880 " + f.length()%2880 + "   (should be 0)") ;
			if (rest  != 0){
				int bytesToAppend = (int) (2880 - f.length()%2880);
				log.debug("Need to append 2880 - f.length()%2880 = " + bytesToAppend  );
				log.debug("appending some bytes now: ");
				FileOutputStream out = new FileOutputStream(f, true);
				byte[] byteArray = new byte[bytesToAppend];
				out.write(byteArray);
				out.close();
				log.debug("Added " + bytesToAppend + " bytes to the file");
				log.debug("Updated Filesize % 2880 " + f.length()%2880 + "   (should be 0)") ;
				}
		}
		StarTable table = ftb.makeStarTable(ds, false, StoragePolicy.ADAPTIVE);

		header = checkHeader(table);
		rowSequence = table.getRowSequence();

		for (int c = 0; c < table.getColumnCount(); c++) {
			ColumnInfo col = table.getColumnInfo(c);
			// log.info( "Adding column '{}'", col.getName() );
			if (!col.getName().trim().equals("")) {
				cols.add(col.getName());
			}
		}
	}

	private Map<String, Class<?>> checkHeader(StarTable table) {
		Map<String, Class<?>> cols = new LinkedHashMap<String, Class<?>>();

		for (int c = 0; c < table.getColumnCount(); c++) {
			ColumnInfo col = table.getColumnInfo(c);
			if (col.getName().trim().isEmpty()) {
				log.debug("Skipping empty column-name '{}' for column {}",
						col.getName(), col);
				continue;
			}

			log.debug("Adding column '{}' of type {}", col.getName(),
					col.getContentClass());

			log.debug("isArray() {} ?? {}", col.getContentClass(), col
					.getContentClass().isArray());
			log.debug("array length is: {}", col.getShape());

			if (col.getContentClass().isArray()) {
				int depth = col.getShape()[0];
				for (int i = 0; i < depth && i < 1024; i++) {
					cols.put(col.getName() + "_" + i, col.getContentClass()
							.getComponentType());
				}
			} else {
				if (col.getName().trim().isEmpty()) {
					continue;
				}
				cols.put(col.getName(), col.getContentClass());
			}
		}
		return cols;
	}

	// /**
	// * @see stream.io.DataStream#getAttributes()
	// */
	// @Override
	// public Map<String, Class<?>> getAttributes() {
	// return this.header;
	// }

	/**
	 * @see stream.io.DataStream#readNext()
	 */
	@Override
	public Data readNext() throws Exception {
		return readNext(DataFactory.create());
	}

	/**
	 * @see stream.io.DataStream#readNext(stream.Data)
	 */
	public Data readNext(Data datum) throws Exception {

		if (limit == 0)
			return null;

		if (rowSequence.next() && (limit < 0 || limit > 0)) {
			// datum.put( "file", fitsFile.getAbsolutePath() );

			Object[] row = rowSequence.getRow();
			for (int c = 0; c < row.length; c++) {
				String colName = cols.get(c);
				Object val = row[c];
				Class<?> clazz = val.getClass();
				if (clazz.isArray()) {

					datum.put(colName, (Serializable) val);
					// dumpArray( val, colName, datum );

				} else {
					datum.put(colName, row[c] + "");
				}
			}

			// FitsConvert.dumpArray(rowSequence.getRow(), "", datum);

			// for (Processor proc : preProcessors) {
			// datum = proc.process(datum);
			// }

			limit--;
			return datum;
		}
		return null;
	}

	public static Data convert(Object[] row, List<String> cols) {
		Data datum = DataFactory.create();
		for (int c = 0; c < row.length; c++) {
			String colName = cols.get(c);
			Object val = row[c];
			Class<?> clazz = val.getClass();
			if (clazz.isArray()) {

				datum.put(colName, (Serializable) val);
				// dumpArray( val, colName, datum );

			} else {
				datum.put(colName, row[c].toString());
			}
		}
		return datum;
	}

	/**
	 * @return the limit
	 */
	public Long getLimit() {
		return limit;
	}

	/**
	 * @param limit
	 *            the limit to set
	 */
	public void setLimit(Long limit) {
		this.limit = limit;
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

		log.error("Don't know how to dump array of {}'s", valueType);
	}

	/**
	 * @see stream.io.DataStream#getPreprocessors()
	 */
	// @Override
	// public List<Processor> getPreprocessors() {
	// return this.preProcessors;
	// }

	// public static void main(String[] args) throws Exception {
	//
	// File testFile = new File("20111120.FSC_CONTROL_TEMPERATURE.fits");
	// testFile = new File("/Users/chris/ISDC-Workshop/20111122_059.fits");
	// testFile = new File("/Volumes/chris/FACT_DATA/20111122_059.fits");
	// FitsDataStream stream = new FitsDataStream(testFile);
	//
	// stream.getPreprocessors().add(new MapKeys("Time", "@time"));
	// stream.getPreprocessors().add(new MJDMapper("@time", "@unixtime"));
	//
	// Data item = stream.readNext();
	// int cnt = 0;
	// CsvWriter dsw = new CsvWriter(new File("events.dat"));
	//
	// while (item != null && cnt++ < 3) {
	// log.info("item: {}", item);
	// item = stream.readNext();
	//
	// short[] slices = (short[]) item.get("Data");
	// short[] startTimes = (short[]) item.get("StartCellData");
	//
	// for (int row = 0; row < 1440; row++) {
	// Data event = DataFactory.create();
	// event.put("eventNum", item.get("EventNum"));
	// event.put("pixel", row + "");
	// // log.info( "Row {} => pixel {}", row, item.get( "pixel" ) );
	// event.put("startCellData", startTimes[row]);
	// for (int s = 0; s < 300; s++) {
	// event.put("slice" + s, slices[row * 300 + s]);
	// }
	// dsw.process(event);
	// }
	// }
	// }

	/**
	 * @see stream.io.DataStream#close()
	 */
	@Override
	public void close() {
	}
}
