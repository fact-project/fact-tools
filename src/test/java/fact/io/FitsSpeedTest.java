/**
 * 
 */
package fact.io;

import static org.junit.Assert.fail;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessorList;
import stream.runtime.ProcessContextImpl;
import uk.ac.starlink.fits.FitsTableBuilder;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StoragePolicy;
import uk.ac.starlink.util.DataSource;
import uk.ac.starlink.util.URLDataSource;
import fact.data.DrsCalibration;

/**
 * @author chris
 * 
 */
public class FitsSpeedTest {

	static Logger log = LoggerFactory.getLogger(FitsSpeedTest.class);
	RowSequence rowSequence;
	List<String> cols = new ArrayList<String>();
	DataSource ds;

	@Test
	public void test() {

		int limit = 100;

		try {
			URL url = new URL("file:/Users/chris/Downloads/20111126_042.fits");
			ds = new URLDataSource(url);

			FitsTableBuilder ftb = new FitsTableBuilder();
			StarTable table = ftb.makeStarTable(ds, false,
					StoragePolicy.ADAPTIVE);

			// header = checkHeader(table);
			rowSequence = table.getRowSequence();

			for (int c = 0; c < table.getColumnCount(); c++) {
				ColumnInfo col = table.getColumnInfo(c);
				// log.info( "Adding column '{}'", col.getName() );
				if (!col.getName().trim().equals("")) {
					cols.add(col.getName());
				}
			}

			ProcessorList preprocess = new ProcessorList();
			preprocess.add(new FactEventStream.Short2FloatData());

			DrsCalibration drs = new DrsCalibration();
			drs.setDrsFile("/Users/chris/Downloads/20111126_034.drs.fits");
			drs.setKeepData(false);
			preprocess.add(drs);

			preprocess.init(new ProcessContextImpl());

			Long start = System.currentTimeMillis();
			int i = 0;
			while (rowSequence.next() && i++ < limit) {

				Data item = processRow(rowSequence.getRow());
				item = preprocess.process(item);
				// if (i > 0 && i % 100 == 0) {
				// log.info("{} rows processed.", i);
				// Long end = System.currentTimeMillis();
				// Double seconds = (end - start) / 1000.0d;
				// log.info("Reading {} rows took {} ms", i, end - start);
				// log.info(" {} rows/sec", limit / seconds.doubleValue());
				// }
			}
			Long end = System.currentTimeMillis();
			Double seconds = (end - start) / 1000.0d;
			log.info("Reading {} rows took {} ms", i, end - start);
			log.info(" {} rows/sec", limit / seconds.doubleValue());

		} catch (Exception e) {
			e.printStackTrace();
			fail("Error: " + e.getMessage());
		}
	}

	public Data processRow(Object[] row) {
		// for (int c = 0; c < row.length; c++) {
		// String colName = cols.get(c);
		// Object val = row[c];
		// Class<?> clazz = val.getClass();
		// if (clazz.isArray()) {
		// // datum.put(colName, (Serializable) val);
		// } else {
		// // datum.put(colName, row[c] + "");
		// }
		// }

		return FitsDataStream.convert(row, cols);
	}
}