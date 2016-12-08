package fact.io;

import fact.io.hdureader.*;

import fact.io.hdureader.zfits.BitQueue;
import fact.io.zfits.ZFitsStream;
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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;


/**
 * Created by mackaiver on 28/10/16.
 */
public class HDUReaderTest {

    static Logger log = LoggerFactory.getLogger(HDUReaderTest.class);

    @Test
    public void testZFits() throws Exception {
        URL u =  FitsStreamTest.class.getResource("/testDataFile.fits.fz");

        Fits f = new Fits(u);

        HDU events = f.getHDU("Events").orElseThrow(IOException::new);

        assertThat(events.header.get("EXTNAME").orElse("WRONG"), is("Events"));

        HDU zDrsCellOffsets = f.getHDU("ZDrsCellOffsets").orElseThrow(IOException::new);

        InputStream inputStreamForHDUData = f.getInputStreamForHDUData(zDrsCellOffsets);

    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testGzipCheck() throws IOException {
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

        assertThat(b.numberOfRowsInTable, is(10749));
        assertThat(b.numberOfColumnsInTable, is(12));
    }


    @Test
    public void testBinTableReader() throws Exception {
        URL u =  FitsStreamTest.class.getResource("/testDataFile.fits.gz");

        Fits f = new Fits(u);

        BinTable b = f.getBinTableByName("Events").orElseThrow(IOException::new);
        TableReader reader = TableReader.forBinTable(b);


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

        //create a new fits file object and get the bin table
        Fits f = new Fits(u);
        BinTable b = f.getBinTableByName("Events").orElseThrow(IOException::new);
        TableReader reader = TableReader.forBinTable(b);

        //init the old fitstream
        FitsStream stream = new FitsStream(new SourceURL(u));
        stream.init();


        Map<String, Serializable> row = reader.getNextRow();
        short[] dataFromBintable = (short[]) row.get("Data");

        Data item = stream.read();
        short[] dataFromOldStream = (short[]) item.get("Data");


        assertArrayEquals(dataFromOldStream, dataFromBintable);

        assertNull(row.get("TimeMarker"));
        assertNull(item.get("TimeMarker"));

    }


    @Test
    public void compareAllEventsFromReaders() throws Exception {
        URL u =  FitsStreamTest.class.getResource("/testDataFile.fits.gz");

        //create a new fits file object and get the bin table
        Fits f = new Fits(u);
        BinTable b = f.getBinTableByName("Events").orElseThrow(IOException::new);
        TableReader reader = TableReader.forBinTable(b);

        //init the old fitstream
        FitsStream stream = new FitsStream(new SourceURL(u));
        stream.init();

        for (int i = 0; i < 15; i++) {
            Map<String, Serializable> row = reader.getNextRow();
            short[] dataFromBintable = (short[]) row.get("Data");

            Data item = stream.read();
            short[] dataFromOldStream = (short[]) item.get("Data");

            assertArrayEquals(dataFromOldStream, dataFromBintable);
        }

    }



    @Test
    public void compareZCalibOffsets() throws Exception {
        URL u =  FitsStreamTest.class.getResource("/testDataFile.fits.fz");


        //new zfitsreader
        Fits f = new Fits(u);
        HDU events = f.getHDU("ZDrsCellOffsets").orElseThrow(IOException::new);
        BinTable binTable = events.getBinTable().orElseThrow(IOException::new);
        ZFitsHeapReader heapReader = ZFitsHeapReader.forTable(binTable);
        NullableMap<String, Serializable> row = heapReader.getNextRow();


        //init the old Zfitstream
        ZFitsStream stream = new ZFitsStream(new SourceURL(u));
        stream.tableName = "Events";
        stream.init();


        assertArrayEquals((short[]) row.get("OffsetCalibration"), stream.calibrationConstants);
    }


    @Test
    public void compareEvents() throws Exception {
        URL u =  FitsStreamTest.class.getResource("/testDataFile.fits.fz");


        //new zfitsreader
        Fits f = new Fits(u);
        HDU events = f.getHDU("Events").orElseThrow(IOException::new);
        BinTable binTable = events.getBinTable().orElseThrow(IOException::new);
        ZFitsHeapReader heapReader = ZFitsHeapReader.forTable(binTable);
        NullableMap<String, Serializable> row = heapReader.getNextRow();
        System.out.println(row);

        //init the old Zfitstream
        ZFitsStream stream = new ZFitsStream(new SourceURL(u));
        stream.tableName = "Events";
        stream.init();
        Data data = stream.readNext();
        System.out.println(data);

    }


    @Test
    public void testZFitsHeapReader() throws Exception {
        URL u =  FitsStreamTest.class.getResource("/testDataFile.fits.fz");

        Fits f = new Fits(u);

        HDU events = f.getHDU("ZDrsCellOffsets").orElseThrow(IOException::new);

        BinTable binTable = events.getBinTable().orElseThrow(IOException::new);
        ZFitsHeapReader heapReader = ZFitsHeapReader.forTable(binTable);
        NullableMap<String, Serializable> row = heapReader.getNextRow();
        System.out.println(row);
    }

    @Test
    public void testOldZFitsHeap() throws Exception {
        URL u =  FitsStreamTest.class.getResource("/testDataFile.fits.fz");

        //init the old fitstream
        ZFitsStream stream = new ZFitsStream(new SourceURL(u));
        stream.tableName = "Events";
        stream.init();
        Data data = stream.readNext();

    }

      @Test
    public void bitQueueTest(){
        BitQueue q = new BitQueue();

        q.addByte(Byte.parseByte("00110011", 2));
        assertThat(q.bitString(), is("00000000"+"00110011"));
        assertThat(q.queueLength, is(8));

        q.addByte(Byte.parseByte("00000000", 2));
        assertThat(q.bitString(), is("00000000" + "00110011"));
        assertThat(q.queueLength, is(16));

        q.remove(2);
        assertThat(q.bitString(), is("00000000" + "00001100"));
        assertThat(q.queueLength, is(14));

        q.remove(8);
        assertThat(q.bitString(), is("00000000" + "00000000"));
        assertThat(q.queueLength, is(6));

        q.addByte(Byte.parseByte("01010101", 2));
        assertThat(q.bitString(), is("00010101" + "01000000"));
        assertThat(q.queueLength, is(14));
        assertThat(q.bitString(), is("00010101" + "01000000"));

    }
}
