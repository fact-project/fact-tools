package fact.io.hdureader;

import fact.Constants;
import fact.Utils;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the bint table and its reader
 * Created by mackaiver on 14/12/16.
 */
public class BinTableTests {

    @Test
    public void testEmptyRepeatCount() throws Exception {
        URL u = BinTableTests.class.getResource("/testRepeatCount.fits.gz");
        FITS f = new FITS(u);
        BinTable table = f.getBinTableByName("Events").orElseThrow(IOException::new);

        for (BinTable.TableColumn column: table.columns) {
            assertEquals(1, column.repeatCount);
        }
    }

    @Test
    public void testBinTableIterator() throws Exception {
        URL u = BinTableTests.class.getResource("/testDataFile.fits.gz");

        FITS f = new FITS(u);
        BinTable events = f.getBinTableByName("Events").orElseThrow(IOException::new);


        for (OptionalTypesMap p : BinTableReader.forBinTable(events)) {
            assertTrue(p.containsKey("Data"));
        }

    }

    @Test
    public void testBinTableIteratorForMCs() throws Exception {
        URL u = BinTableTests.class.getResource("/testMcFile.fits.gz");

        FITS f = new FITS(u);
        BinTable events = f.getBinTableByName("Events").orElseThrow(IOException::new);


        for (OptionalTypesMap p : BinTableReader.forBinTable(events)) {
            assertTrue(p.containsKey("Data"));
        }

    }


    @Test
    public void testBinTableReader() throws Exception {
        URL u = BinTableTests.class.getResource("/testDataFile.fits.gz");

        FITS f = new FITS(u);

        BinTable b = f.getBinTableByName("Events").orElseThrow(IOException::new);
        BinTableReader reader = BinTableReader.forBinTable(b);


        Map<String, Serializable> row = reader.getNextRow();
        assertThat(row.size(), is(b.numberOfColumnsInTable));

        short[] data = (short[]) row.get("Data");
        assertThat(data.length, is(Constants.N_PIXELS * 300));


        int[] boardTime = (int[]) row.get("BoardTime");
        assertThat(boardTime.length, is(40));


        int[] unixtime = (int[]) row.get("UnixTimeUTC");
        assertThat(unixtime.length, is(2));

        ZonedDateTime date = Utils.unixTimeUTCToZonedDateTime(unixtime);
        ;
        assertThat(date.getYear(), is(2013));
        assertThat(date.getMonthValue(), is(1));
        assertThat(date.getDayOfMonth(), is(2));
        assertThat(date.getHour(), is(21));
        assertThat(date.getMinute(), is(46));
    }

    @Test
    public void testFitsBinTable() throws Exception {
        URL u = BinTableTests.class.getResource("/testDataFile.fits.gz");

        FITS f = new FITS(u);

        BinTable b = f.getBinTableByName("Events").orElseThrow(IOException::new);

        assertThat(b.numberOfRowsInTable, is(15));
        assertThat(b.numberOfColumnsInTable, is(12));
    }
}
