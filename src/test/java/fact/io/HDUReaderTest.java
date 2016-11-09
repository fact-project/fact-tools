package fact.io;

import com.google.common.io.ByteStreams;
import fact.io.hdureader.BinTable;
import fact.io.hdureader.Fits;
import fact.io.hdureader.HDU;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.io.SourceURL;

import java.io.*;
import java.net.URL;

import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;


/**
 * Created by mackaiver on 28/10/16.
 */
public class HDUReaderTest {

    static Logger log = LoggerFactory.getLogger(HDUReaderTest.class);


    @Test
    public void findHDUs() throws IOException {
        URL u =  FitsStreamTest.class.getResource("/testDataFile.fits.fz");
        DataInputStream stream = new DataInputStream(new BufferedInputStream(u.openStream()));

        HDU h = new HDU(stream);
        System.out.println(h);
        ByteStreams.skipFully(stream, h.offsetToNextHDU());

        h = new HDU(stream);
        System.out.println(h);
        ByteStreams.skipFully(stream, h.offsetToNextHDU());

        System.out.println("size: " + h.sizeOfDataArea() + "  offset: " + h.offsetToNextHDU() + " bytes");

        h = new HDU(stream);
        System.out.println(h);
    }

    @Test
    public void findHDUsGzip() throws IOException {
        URL u =  FitsStreamTest.class.getResource("/testDataFile.fits.gz");
        DataInputStream stream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(u.openStream())));

        HDU h = new HDU(stream);
        System.out.println(h);
        ByteStreams.skipFully(stream, h.offsetToNextHDU());

        h = new HDU(stream);
        System.out.println(h);

        System.out.println("size: " + h.sizeOfDataArea() + "  offset: " + h.offsetToNextHDU() + " bytes");
    }

    @Test
    public void testZFits() throws Exception {
        URL u =  FitsStreamTest.class.getResource("/testDataFile.fits.fz");

        Fits f = new Fits(u);

        HDU events = f.getHDU("Events").orElseThrow(IOException::new);
        HDU zDrsCellOffsets = f.getHDU("ZDrsCellOffsets").orElseThrow(IOException::new);

        InputStream inputStreamForHDUData = f.getInputStreamForHDUData(zDrsCellOffsets);
        int b = new DataInputStream(inputStreamForHDUData).read();

    }


    @Test
    public void testGzip() throws IOException {
        URL u =  FitsStreamTest.class.getResource("/testDataFile.fits.gz");

        byte[] header = new byte[2];
        u.openStream().read(header);

        assertTrue(Fits.isGzippedCompressed(header));


        u =  FitsStreamTest.class.getResource("/testDataFile.fits.fz");

        header = new byte[2];
        u.openStream().read(header);

        assertFalse(Fits.isGzippedCompressed(header));
    }

    @Test
    public void testFits() throws Exception {
        URL u =  FitsStreamTest.class.getResource("/testDataFile.fits.gz");

        Fits f = new Fits(u);

        HDU events = f.getHDU("Events").orElseThrow(IOException::new);

        DataInputStream inputStreamForHDUData = f.getInputStreamForHDUData(events);

        int eventNum = inputStreamForHDUData.readInt();

        assertThat(eventNum, is(1));


    }


    @Test
    public void testFitsBinTable() throws Exception {
        URL u =  FitsStreamTest.class.getResource("/testDataFile.fits.gz");

        Fits f = new Fits(u);

        BinTable b = f.getBinTableByName("Events").orElseThrow(IOException::new);

//        HDU events = f.getHDU("Events").orElseThrow(IOException::new);

//        BinTable b = new BinTable(events, f.getInputStreamForHDUData(events));
        assertThat(b.numberOfRowsInTable, is(10749));
        assertThat(b.numberOfColumnsInTable, is(12));
    }


    @Test
    public void testBinTableReader() throws Exception {
        URL u =  FitsStreamTest.class.getResource("/testDataFile.fits.gz");

        Fits f = new Fits(u);

        BinTable b = f.getBinTableByName("Events").orElseThrow(IOException::new);
        BinTable.Reader reader = b.reader;

        Map<String, Serializable> row = reader.getNextRow();
        assertThat(row.size() , is(b.numberOfColumnsInTable));

        short[] data = (short[]) row.get("Data");
        assertThat(data.length , is(1440*300));


        int[] boardTime = (int[]) row.get("BoardTime");
        assertThat(boardTime.length , is(40));


        int[] unixtime = (int[]) row.get("UnixTimeUTC");
        assertThat(unixtime.length , is(2));

        DateTime date = new DateTime((long) (unixtime[0]* 1000.0 + unixtime[1]/ 1000.0),  DateTimeZone.UTC);
        assertThat(date.getYear(), is(2013));
        assertThat(date.getMonthOfYear(), is(1));
        assertThat(date.getDayOfMonth(), is(2));
        assertThat(date.getHourOfDay(), is(21));
        assertThat(date.getMinuteOfHour(), is(46));
    }

    @Test
    public void compareReaders() throws Exception {
        URL u =  FitsStreamTest.class.getResource("/testDataFile.fits.gz");

        Fits f = new Fits(u);
        BinTable b = f.getBinTableByName("Events").orElseThrow(IOException::new);
        BinTable.Reader reader = b.reader;
        Map<String, Serializable> row = reader.getNextRow();

        short[] datFromBintable = (short[]) row.get("Data");



        FitsStream stream = new FitsStream(new SourceURL(u));
        stream.init();
        Data item = stream.read();

        short[] dataFromOldStream = (short[]) item.get("Data");


        assertArrayEquals(dataFromOldStream, datFromBintable);
    }
}
