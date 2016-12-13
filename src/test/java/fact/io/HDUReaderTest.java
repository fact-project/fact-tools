package fact.io;


import fact.io.hdureader.*;

import fact.io.hdureader.zfits.BitQueue;
import fact.io.zfits.ZFitsStream;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import stream.Data;
import stream.io.SourceURL;

import java.io.*;
import java.net.URL;

import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;


/**
 * Tests several aspects of the hdu reader.
 * Created by mackaiver on 28/10/16.
 */
public class HDUReaderTest {


    @Test
    public void testToOpenInputStream() throws Exception {
        URL u =  HDUReaderTest.class.getResource("/testDataFile.fits.fz");

        Fits f = new Fits(u);

        HDU events = f.getHDU("Events");

        assertThat(events.header.get("EXTNAME").orElse("WRONG"), is("Events"));

        HDU zDrsCellOffsets = f.getHDU("ZDrsCellOffsets");

        f.getInputStreamForHDUData(zDrsCellOffsets);

    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testGzipCheck() throws IOException {
        URL u =  HDUReaderTest.class.getResource("/testDataFile.fits.gz");

        byte[] header = new byte[2];
        u.openStream().read(header);

        assertTrue(Fits.isGzippedCompressed(header));


        u =  HDUReaderTest.class.getResource("/testDataFile.fits.fz");

        header = new byte[2];
        u.openStream().read(header);

        assertFalse(Fits.isGzippedCompressed(header));
    }

    @Test
    public void testInputStreamFromFitsFile() throws Exception {
        URL u =  HDUReaderTest.class.getResource("/testDataFile.fits.gz");

        Fits f = new Fits(u);

        HDU events = f.getHDU("Events");

        DataInputStream inputStreamForHDUData = f.getInputStreamForHDUData(events);

        int eventNum = inputStreamForHDUData.readInt();

        assertThat(eventNum, is(1));
    }



    @Test
    public void testPrimaryHDU() throws Exception {
        URL u =  HDUReaderTest.class.getResource("/testDataFile.fits.gz");

        Fits f = new Fits(u);

        HDU primaryHDU = f.primaryHDU;

        assertTrue(primaryHDU.isPrimaryHDU);

        String checkSum = primaryHDU.header.get("CHECKSUM").orElse("Wrong");

        assertThat(checkSum, is("4AcB48bA4AbA45bA"));
    }

    @Test
    public void testFitsBinTable() throws Exception {
        URL u =  HDUReaderTest.class.getResource("/testDataFile.fits.gz");

        Fits f = new Fits(u);

        BinTable b = f.getBinTableByName("Events").orElseThrow(IOException::new);

        assertThat(b.numberOfRowsInTable, is(15));
        assertThat(b.numberOfColumnsInTable, is(12));
    }


    @Test
    public void testBinTableReader() throws Exception {
        URL u =  HDUReaderTest.class.getResource("/testDataFile.fits.gz");

        Fits f = new Fits(u);

        BinTable b = f.getBinTableByName("Events").orElseThrow(IOException::new);
        BinTableReader reader = BinTableReader.forBinTable(b);


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
        URL u =  HDUReaderTest.class.getResource("/testDataFile.fits.gz");

        //create a new fits file object and get the bin table
        Fits f = new Fits(u);
        BinTable b = f.getBinTableByName("Events").orElseThrow(IOException::new);
        BinTableReader reader = BinTableReader.forBinTable(b);

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
        URL u =  HDUReaderTest.class.getResource("/testDataFile.fits.gz");

        //create a new fits file object and get the bin table
        Fits f = new Fits(u);
        BinTable b = f.getBinTableByName("Events").orElseThrow(IOException::new);
        BinTableReader reader = BinTableReader.forBinTable(b);

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
        URL u =  HDUReaderTest.class.getResource("/testDataFile.fits.fz");


        //new zfitsreader
        Fits f = new Fits(u);
        HDU events = f.getHDU("ZDrsCellOffsets");
        BinTable binTable = events.getBinTable();
        ZFitsHeapReader heapReader = ZFitsHeapReader.forTable(binTable);
        OptionalTypesMap<String, Serializable> row = heapReader.getNextRow();


        //init the old Zfitstream
        ZFitsStream stream = new ZFitsStream(new SourceURL(u));
        stream.tableName = "Events";
        stream.init();


        assertArrayEquals((short[]) row.get("OffsetCalibration"), stream.calibrationConstants);
    }


    @Test
    public void compareFirstZfitsEvent() throws Exception {
        URL u =  HDUReaderTest.class.getResource("/testDataFile.fits.fz");

        //init the old Zfitstream
        ZFitsStream stream = new ZFitsStream(new SourceURL(u));
        stream.tableName = "Events";
        stream.init();
        Data dataFromOldStream = stream.readNext();

        //new zfitsreader
        Fits f = new Fits(u);
        HDU events = f.getHDU("Events");
        BinTable binTable = events.getBinTable();
        ZFitsHeapReader heapReader = ZFitsHeapReader.forTable(binTable);
        OptionalTypesMap<String, Serializable> data = heapReader.getNextRow();

        assertTrue(data.size() > 0);
        assertArrayEquals((int[]) data.get("BoardTime"), (int[]) dataFromOldStream.get("BoardTime"));
        assertArrayEquals((short[]) data.get("StartCellTimeMarker"), (short[]) dataFromOldStream.get("StartCellTimeMarker"));
        assertArrayEquals((short[]) data.get("StartCellData"), (short[]) dataFromOldStream.get("StartCellData"));
    }


    @Test
    public void compareFirstItemFromZfitsStreams() throws Exception {
        URL u =  HDUReaderTest.class.getResource("/testDataFile.fits.fz");

        //init the old Zfitstream
        ZFitsStream stream = new ZFitsStream(new SourceURL(u));
        stream.tableName = "Events";
        stream.init();


        fact.io.hdureader.ZFitsStream newStream = new fact.io.hdureader.ZFitsStream(new SourceURL(u));
        newStream.init();

        Data dataFromOldStream = stream.readNext();

        Data data = newStream.readNext();


        assertTrue(data.size() > 0);
        assertArrayEquals((int[]) data.get("BoardTime"), (int[]) dataFromOldStream.get("BoardTime"));
        assertArrayEquals((short[]) data.get("StartCellTimeMarker"), (short[]) dataFromOldStream.get("StartCellTimeMarker"));
        assertArrayEquals((short[]) data.get("StartCellData"), (short[]) dataFromOldStream.get("StartCellData"));

        short[] newShorts = Arrays.copyOfRange((short[]) data.get("Data"), 0, 431700);
        short[] oldShorts = Arrays.copyOfRange((short[]) dataFromOldStream.get("Data"), 0, 431700);
        assertArrayEquals(oldShorts, newShorts);
    }

    @Test
    public void compareZfitsStreams() throws Exception {
        URL u =  HDUReaderTest.class.getResource("/testDataFile.fits.fz");

        //init the old Zfitstream
        ZFitsStream stream = new ZFitsStream(new SourceURL(u));
        stream.tableName = "Events";
        stream.init();


        fact.io.hdureader.ZFitsStream newStream = new fact.io.hdureader.ZFitsStream(new SourceURL(u));
        newStream.init();
        int i = 0;
        while(true) {
//            System.out.println("checking event number: " + i++);
            Data dataFromOldStream = stream.readNext();

            Data data = newStream.readNext();

            if (dataFromOldStream == null && data == null){
                break;
            }

            assertTrue(data.size() > 0);
            assertArrayEquals((int[]) data.get("BoardTime"), (int[]) dataFromOldStream.get("BoardTime"));
            assertArrayEquals((short[]) data.get("StartCellTimeMarker"), (short[]) dataFromOldStream.get("StartCellTimeMarker"));
            assertArrayEquals((short[]) data.get("StartCellData"), (short[]) dataFromOldStream.get("StartCellData"));

//            assertArrayEquals((short[]) data.get("Data"), (short[]) dataFromOldStream.get("Data"));

            short[] newShorts = Arrays.copyOfRange((short[]) data.get("Data"), 0, 431700);
            short[] oldShorts = Arrays.copyOfRange((short[]) dataFromOldStream.get("Data"), 0, 431700);
            assertArrayEquals(oldShorts, newShorts);

        }

    }


    @Test
    public void testHeapIterator() throws Exception {
        URL u =  HDUReaderTest.class.getResource("/testDataFile.fits.fz");

        Fits f = new Fits(u);
        HDU events = f.getHDU("Events");
        BinTable binTable = events.getBinTable();
        ZFitsHeapReader heapReader = ZFitsHeapReader.forTable(binTable);

        for(OptionalTypesMap p : heapReader){
            assertTrue(p.containsKey("Data"));
            assertTrue(p.size() == 9);
        }

    }

    @Test
    public void testBinTableIterator() throws Exception {
        URL u =  HDUReaderTest.class.getResource("/testDataFile.fits.gz");

        Fits f = new Fits(u);
        BinTable events = f.getBinTableByName("Events").orElseThrow(IOException::new);


        for(OptionalTypesMap p : BinTableReader.forBinTable(events)){
            assertTrue(p.containsKey("Data"));
        }

    }


    @Test
    public void compareAllZfitsEvents() throws Exception {
        URL u =  HDUReaderTest.class.getResource("/testDataFile.fits.fz");

        //init the old Zfitstream
        ZFitsStream stream = new ZFitsStream(new SourceURL(u));
        stream.tableName = "Events";
        stream.applyOffsetCalibration = false;
        stream.init();

        //new zfitsreader
        Fits f = new Fits(u);
        HDU events = f.getHDU("Events");
        BinTable binTable = events.getBinTable();
        ZFitsHeapReader heapReader = ZFitsHeapReader.forTable(binTable);

        for (int i = 0; i < 5; i++) {
            Data dataFromOldStream = stream.readNext();

            OptionalTypesMap<String, Serializable> data = heapReader.getNextRow();

            assertTrue(data.size() > 0);
            assertArrayEquals((int[]) dataFromOldStream.get("BoardTime"), (int[]) data.get("BoardTime"));
            assertArrayEquals((short[]) dataFromOldStream.get("StartCellTimeMarker"), (short[]) data.get("StartCellTimeMarker"));
            assertArrayEquals((short[]) dataFromOldStream.get("StartCellData"), (short[]) data.get("StartCellData"));
            assertArrayEquals((short[]) dataFromOldStream.get("Data"), (short[]) data.get("Data"));
        }

    }


    @Test
    public void testZFitsHeapReader() throws Exception {
        URL u =  HDUReaderTest.class.getResource("/testDataFile.fits.fz");

        Fits f = new Fits(u);

        HDU events = f.getHDU("ZDrsCellOffsets");

        BinTable binTable = events.getBinTable();
        ZFitsHeapReader heapReader = ZFitsHeapReader.forTable(binTable);
        OptionalTypesMap<String, Serializable> row = heapReader.getNextRow();
        assertTrue(row.size() > 0);
        assertTrue(row.containsKey("OffsetCalibration"));

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
