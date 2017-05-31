package fact.io.hdureader;


import fact.io.FITSStream;

import fact.io.zfits.ZFitsStream;
import org.junit.Test;

import stream.Data;
import stream.io.SourceURL;

import java.io.*;
import java.net.URL;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;


/**
 * Tests several aspects of the hdu reader.
 * Created by mackaiver on 28/10/16.
 */
public class CompareOldAndNewReaders {



    @Test
    public void compareReaders() throws Exception {
        URL u =  CompareOldAndNewReaders.class.getResource("/testDataFile.fits.gz");

        //create a new fits file object and get the bin table
        FITS f = new FITS(u);
        BinTable b = f.getBinTableByName("Events").orElseThrow(IOException::new);
        BinTableReader reader = BinTableReader.forBinTable(b);

        //init the old fitstream
        FITSStream stream = new FITSStream(new SourceURL(u));
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
        URL u =  CompareOldAndNewReaders.class.getResource("/testDataFile.fits.gz");

        //create a new fits file object and get the bin table
        FITS f = new FITS(u);
        BinTable b = f.getBinTableByName("Events").orElseThrow(IOException::new);
        BinTableReader reader = BinTableReader.forBinTable(b);

        //init the old fitstream
        FITSStream stream = new FITSStream(new SourceURL(u));
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
    public void compareAllEventsFromReadersForMcFile() throws Exception {
        URL u =  CompareOldAndNewReaders.class.getResource("/testMcFile.fits.gz");

        //create a new fits file object and get the bin table
        FITS f = new FITS(u);
        BinTable b = f.getBinTableByName("Events").orElseThrow(IOException::new);
        BinTableReader reader = BinTableReader.forBinTable(b);

        //init the old fitstream
        FITSStream stream = new FITSStream(new SourceURL(u));
        stream.init();

        for (int i = 0; i < 15; i++) {
            Map<String, Serializable> row = reader.getNextRow();
            Data item = stream.read();


            short[] dataFromBintable = (short[]) row.get("Data");
            short[] dataFromOldStream = (short[]) item.get("Data");

            assertArrayEquals(dataFromOldStream, dataFromBintable);
        }

    }



    @Test
    public void compareZCalibOffsets() throws Exception {
        URL u =  CompareOldAndNewReaders.class.getResource("/testDataFile.fits.fz");


        //new zfitsreader
        FITS f = new FITS(u);
        HDU events = f.getHDU("ZDrsCellOffsets");
        BinTable binTable = events.getBinTable();
        ZFITSHeapReader heapReader = ZFITSHeapReader.forTable(binTable);
        OptionalTypesMap<String, Serializable> row = heapReader.getNextRow();


        //init the old Zfitstream
        ZFitsStream stream = new ZFitsStream(new SourceURL(u));
        stream.tableName = "Events";
        stream.init();


        assertArrayEquals((short[]) row.get("OffsetCalibration"), stream.calibrationConstants);
    }


    @Test
    public void compareFirstZfitsEvent() throws Exception {
        URL u =  CompareOldAndNewReaders.class.getResource("/testDataFile.fits.fz");

        //init the old Zfitstream
        ZFitsStream stream = new ZFitsStream(new SourceURL(u));
        stream.tableName = "Events";
        stream.init();
        Data dataFromOldStream = stream.readNext();

        //new zfitsreader
        FITS f = new FITS(u);
        HDU events = f.getHDU("Events");
        BinTable binTable = events.getBinTable();
        ZFITSHeapReader heapReader = ZFITSHeapReader.forTable(binTable);
        OptionalTypesMap<String, Serializable> data = heapReader.getNextRow();

        assertTrue(data.size() > 0);
        assertArrayEquals((int[]) data.get("BoardTime"), (int[]) dataFromOldStream.get("BoardTime"));
        assertArrayEquals((short[]) data.get("StartCellTimeMarker"), (short[]) dataFromOldStream.get("StartCellTimeMarker"));
        assertArrayEquals((short[]) data.get("StartCellData"), (short[]) dataFromOldStream.get("StartCellData"));
    }


    @Test
    public void compareFirstItemFromZfitsStreams() throws Exception {
        URL u =  CompareOldAndNewReaders.class.getResource("/testDataFile.fits.fz");

        //init the old Zfitstream
        ZFitsStream stream = new ZFitsStream(new SourceURL(u));
        stream.tableName = "Events";
        stream.init();


        fact.io.hdureader.FITSStream newStream = new fact.io.hdureader.FITSStream(new SourceURL(u));
        newStream.init();

        Data dataFromOldStream = stream.readNext();

        Data data = newStream.readNext();


        assertTrue(data.size() > 0);
        assertArrayEquals((int[]) data.get("BoardTime"), (int[]) dataFromOldStream.get("BoardTime"));
        assertArrayEquals((short[]) data.get("StartCellTimeMarker"), (short[]) dataFromOldStream.get("StartCellTimeMarker"));
        assertArrayEquals((short[]) data.get("StartCellData"), (short[]) dataFromOldStream.get("StartCellData"));
        assertArrayEquals((short[]) dataFromOldStream.get("Data"), (short[]) data.get("Data"));
    }

    @Test
    public void compareStreamsForZFitsFiles() throws Exception {
        URL u =  CompareOldAndNewReaders.class.getResource("/testDataFile.fits.fz");

        //init the old Zfitstream
        ZFitsStream stream = new ZFitsStream(new SourceURL(u));
        stream.tableName = "Events";
        stream.init();


        fact.io.hdureader.FITSStream newStream = new fact.io.hdureader.FITSStream(new SourceURL(u));
        newStream.init();
        int i = 0;
        while(true) {
            Data dataFromOldStream = stream.readNext();

            Data data = newStream.readNext();

            if (dataFromOldStream == null && data == null){
                break;
            }

            //compare header items
            assertEquals(1.0, data.get("EXTREL"));

            assertEquals(1440, data.get("NPIX"));
            assertEquals(dataFromOldStream.get("NPIX"), data.get("NPIX"));


            //compare data
            assertTrue(data.size() > 0);
            assertArrayEquals((int[]) data.get("BoardTime"), (int[]) dataFromOldStream.get("BoardTime"));
            assertArrayEquals((short[]) data.get("StartCellTimeMarker"), (short[]) dataFromOldStream.get("StartCellTimeMarker"));
            assertArrayEquals((short[]) data.get("StartCellData"), (short[]) dataFromOldStream.get("StartCellData"));

            assertArrayEquals((short[]) data.get("Data"), (short[]) dataFromOldStream.get("Data"));
        }

    }

    @Test
    public void compareStreamsForFitsFiles() throws Exception {
        URL u =  CompareOldAndNewReaders.class.getResource("/testDataFile.fits.gz");

        //init the old Zfitstream
        ZFitsStream stream = new ZFitsStream(new SourceURL(u));
        stream.tableName = "Events";
        stream.init();


        fact.io.hdureader.FITSStream newStream = new fact.io.hdureader.FITSStream(new SourceURL(u));
        newStream.init();

        for (int i = 0; i < 15; i++) {
            Data dataFromOldStream = stream.readNext();

            Data data = newStream.readNext();

            //compare header items
            assertEquals(1.0, data.get("EXTREL"));

            assertEquals(1440, data.get("NPIX"));
            assertEquals(dataFromOldStream.get("NPIX"), data.get("NPIX"));


            //compare data
            assertTrue(data.size() > 0);
            assertArrayEquals((int[]) data.get("BoardTime"), (int[]) dataFromOldStream.get("BoardTime"));
            assertArrayEquals((short[]) data.get("StartCellTimeMarker"), (short[]) dataFromOldStream.get("StartCellTimeMarker"));
            assertArrayEquals((short[]) data.get("StartCellData"), (short[]) dataFromOldStream.get("StartCellData"));

            assertArrayEquals((short[]) data.get("Data"), (short[]) dataFromOldStream.get("Data"));
        }

    }




    @Test
    public void compareAllZfitsEvents() throws Exception {
        URL u =  CompareOldAndNewReaders.class.getResource("/testDataFile.fits.fz");

        //init the old Zfitstream
        ZFitsStream stream = new ZFitsStream(new SourceURL(u));
        stream.tableName = "Events";
        stream.applyOffsetCalibration = false;
        stream.init();

        //new zfitsreader
        FITS f = new FITS(u);
        HDU events = f.getHDU("Events");
        BinTable binTable = events.getBinTable();
        ZFITSHeapReader heapReader = ZFITSHeapReader.forTable(binTable);

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
        URL u =  CompareOldAndNewReaders.class.getResource("/testDataFile.fits.fz");

        FITS f = new FITS(u);

        HDU events = f.getHDU("ZDrsCellOffsets");

        BinTable binTable = events.getBinTable();
        ZFITSHeapReader heapReader = ZFITSHeapReader.forTable(binTable);
        OptionalTypesMap<String, Serializable> row = heapReader.getNextRow();
        assertTrue(row.size() > 0);
        assertTrue(row.containsKey("OffsetCalibration"));

    }


}
