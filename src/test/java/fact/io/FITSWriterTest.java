/**
 *
 */
package fact.io;

import fact.io.hdureader.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Keys;
import stream.data.DataFactory;
import java.io.File;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;


/**
 * @author maxnoe
 */
public class FITSWriterTest {
    static Logger log = LoggerFactory.getLogger(FITSWriterTest.class);



	@Test
	public void testFitsWriterNoItems() throws Exception {
        FITSWriter fitsWriter = new FITSWriter();
        File f = File.createTempFile("test_fits", ".fits");
        log.info("testFitsWriterNoItems {}", f.getAbsolutePath());

        URL url = new URL("file:" + f.getAbsolutePath());
        fitsWriter.url = url;
        fitsWriter.init(null);
        fitsWriter.finish();
    }

    @Test
    public void testFitsWriter() throws Exception {

        File f = File.createTempFile("test_fits", ".fits");
        log.info("testFitsWriter with path {}", f.getAbsolutePath());

        URL url = new URL("file:" + f.getAbsolutePath());

        FITSWriter fitsWriter = new FITSWriter();
        fitsWriter.url = url;
        fitsWriter.keys = new Keys("*");
        fitsWriter.init(null);

        Data item = DataFactory.create();
        item.put("EventNum", 1);
        item.put("TriggerType", 4);
        item.put("NROI", 300);
        item.put("NPIX", 1440);
        item.put("x", 0.0);
        item.put("y", 5.0);

        double[] array = new double[]{1.0, 2.0, 3.0};
        item.put("array", array);

        item.put("timestamp", ZonedDateTime.of(2017, 1, 1, 12, 0, 0, 991300000, ZoneOffset.UTC));

        fitsWriter.process(item);
        assertEquals(fitsWriter.numEventsWritten, 1);
        fitsWriter.finish();

        FITS fits = FITS.fromFile(f);
        BinTable table = fits.getBinTableByName("Events").get();
        BinTableReader tableReader = BinTableReader.forBinTable(table);
        OptionalTypesMap row = tableReader.getNextRow();

        assertEquals(row.getInt("NROI").get(), 300);
        assertEquals(row.getInt("NPIX").get(), 1440);
        assertEquals(row.getDouble("x").get(), 0.0);
        assertEquals(row.getDouble("y").get(), 5.0);

        double[] arrayRead = (double[]) row.getDoubleArray("array").get();
        for (int i = 0; i < array.length; i++) {
            assertEquals(array[i], arrayRead[i], 1e-12);
        }
        assertEquals(row.get("timestamp"), "2017-01-01T12:00:00.991300Z");
    }

    @Test
    public void testHeaderKeys() throws Exception {
        File f = File.createTempFile("test_fitsheader", ".fits");
        log.info("testHeaderKeys with path {}", f.getAbsolutePath());

        URL url = new URL("file:" + f.getAbsolutePath());

        FITSWriter fitsWriter = new FITSWriter();
        fitsWriter.url = url;
        fitsWriter.keys = new Keys("EventNum,TriggerType");
        fitsWriter.headerKeys = new Keys("NROI,NPIX,UTCTIME,muchtoolong");
        fitsWriter.init(null);

        Data item = DataFactory.create();
        item.put("muchtoolong", true);
        item.put("EventNum", 1);
        item.put("TriggerType", 4);
        item.put("NROI", 300);
        item.put("NPIX", 1440);
        item.put("UTCTIME", ZonedDateTime.of(2017, 1, 1, 12, 0, 0, 991300000, ZoneOffset.UTC));


        fitsWriter.process(item);
        assertEquals(fitsWriter.numEventsWritten, 1);
        fitsWriter.finish();

        FITS fits = FITS.fromFile(f);

        HDU hdu = fits.getHDU("Events");

        Header header = hdu.header;

        // test truncation of too long key
        assertEquals(true, header.getBoolean("muchtool").get());

        int nroi = header.getInt("NROI").get();
        assertEquals(300, nroi);

        assertEquals("2017-01-01T12:00:00.991300Z", header.get("UTCTIME").get());

        int npix = header.getInt("NPIX").get();
        assertEquals(1440, npix);

        assertEquals(false, header.get("EventNum").isPresent());
    }

    @Test
    public void testDateTimeFormatter () {
	    ZonedDateTime date1 = ZonedDateTime.of(2013, 11, 01, 23, 44, 25, 123000000, ZoneOffset.UTC);
	    ZonedDateTime date2 = ZonedDateTime.of(2013, 11, 01, 23, 44, 25, 123400000, ZoneOffset.UTC);
	    ZonedDateTime date3 = ZonedDateTime.of(2013, 11, 01, 23, 44, 25, 123456000, ZoneOffset.UTC);
	    ZonedDateTime date4 = ZonedDateTime.of(2013, 11, 01, 23, 44, 25, 123456700, ZoneOffset.UTC);
	    ZonedDateTime date5 = ZonedDateTime.of(2013, 11, 01, 23, 44, 25, 123456789, ZoneOffset.UTC);

	    assertEquals(FITSWriter.formatDateTime(date1).length(), 27);
	    assertEquals(FITSWriter.formatDateTime(date2).length(), 27);
	    assertEquals(FITSWriter.formatDateTime(date3).length(), 27);
	    assertEquals(FITSWriter.formatDateTime(date4).length(), 27);
	    assertEquals(FITSWriter.formatDateTime(date5).length(), 27);
    }
}
