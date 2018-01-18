/**
 *
 */
package fact.io;

import fact.Constants;
import fact.io.hdureader.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Keys;
import stream.data.DataFactory;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.Assert.*;


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
        item.put("NPIX", Constants.N_PIXELS);
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
        OptionalTypesMap<String, Serializable> row = tableReader.getNextRow();

        assertEquals(300, (int) row.getInt("NROI").orElse(-1));
        assertEquals(Constants.N_PIXELS, (int) row.getInt("NPIX").orElse(-1));
        assertEquals(0.0, row.getDouble("x").get(), 1e-12);
        assertEquals(5.0, row.getDouble("y").get(), 1e-12);

        double[] arrayRead = row.getDoubleArray("array").orElse(new double[]{});
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
        item.put("NPIX", Constants.N_PIXELS);
        item.put("UTCTIME", ZonedDateTime.of(2017, 1, 1, 12, 0, 0, 991300000, ZoneOffset.UTC));


        fitsWriter.process(item);
        assertEquals(fitsWriter.numEventsWritten, 1);
        fitsWriter.finish();

        FITS fits = FITS.fromFile(f);

        HDU hdu = fits.getHDU("Events").orElseThrow(() -> new RuntimeException("File did not contain HDU 'Events'"));

        Header header = hdu.header;

        // test truncation of too long key
        Optional<Boolean> muchtool = header.getBoolean("muchtool");
        assertTrue(muchtool.isPresent());
        assertEquals(true, muchtool.get());

        assertEquals(300, (int) header.getInt("NROI").orElse(-1));

        assertEquals("2017-01-01T12:00:00.991300Z", header.get("UTCTIME").orElse(""));

        assertEquals(Constants.N_PIXELS, (int) header.getInt("NPIX").orElse(-1));

        assertFalse(header.get("EventNum").isPresent());
    }

    @Test
    public void testDateTimeFormatter() {
        ZonedDateTime date1 = ZonedDateTime.of(2013, 11, 01, 23, 44, 25, 123000000, ZoneOffset.UTC);
        ZonedDateTime date2 = ZonedDateTime.of(2013, 11, 01, 23, 44, 25, 123400000, ZoneOffset.UTC);
        ZonedDateTime date3 = ZonedDateTime.of(2013, 11, 01, 23, 44, 25, 123456000, ZoneOffset.UTC);
        ZonedDateTime date4 = ZonedDateTime.of(2013, 11, 01, 23, 44, 25, 123456700, ZoneOffset.UTC);
        ZonedDateTime date5 = ZonedDateTime.of(2013, 11, 01, 23, 44, 25, 123456789, ZoneOffset.UTC);
        ZonedDateTime date6 = ZonedDateTime.of(2013, 11, 01, 0, 0, 0, 0, ZoneOffset.UTC);

        assertEquals(FITSWriter.formatDateTime(date1).length(), 27);
        assertEquals(FITSWriter.formatDateTime(date2).length(), 27);
        assertEquals(FITSWriter.formatDateTime(date3).length(), 27);
        assertEquals(FITSWriter.formatDateTime(date4).length(), 27);
        assertEquals(FITSWriter.formatDateTime(date5).length(), 27);
        assertEquals(FITSWriter.formatDateTime(date6).length(), 27);

        assertEquals(FITSWriter.formatDateTime(date1), "2013-11-01T23:44:25.123000Z");
        assertEquals(FITSWriter.formatDateTime(date2), "2013-11-01T23:44:25.123400Z");
        assertEquals(FITSWriter.formatDateTime(date3), "2013-11-01T23:44:25.123456Z");
        assertEquals(FITSWriter.formatDateTime(date4), "2013-11-01T23:44:25.123456Z");
        assertEquals(FITSWriter.formatDateTime(date5), "2013-11-01T23:44:25.123456Z");
        assertEquals(FITSWriter.formatDateTime(date6), "2013-11-01T00:00:00.000000Z");
    }
}
