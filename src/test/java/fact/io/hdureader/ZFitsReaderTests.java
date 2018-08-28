package fact.io.hdureader;

import org.junit.Test;

import java.io.Serializable;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the basics of the zfitsheapreader
 * <p>
 * Created by mackaiver on 14/12/16.
 */
public class ZFitsReaderTests {

    @Test
    public void testHeapIterator() throws Exception {
        //URL u =  CompareOldAndNewReaders.class.getResource("/testDataFile.fits.fz");
        URL u = ZFitsReaderTests.class.getResource("/testDataFile.fits.fz");

        FITS f = new FITS(u);
        HDU events = f.getHDU("Events").orElseThrow(() -> new RuntimeException("File did not contain HDU 'Events'"));
        BinTable binTable = events.getBinTable();
        ZFITSHeapReader heapReader = ZFITSHeapReader.forTable(binTable);

        for (OptionalTypesMap p : heapReader) {
            assertTrue(p.containsKey("Data"));
            assertTrue(p.size() == 9);
        }
    }

    @Test
    public void testZSHRINK() throws Exception {
        //URL u =  CompareOldAndNewReaders.class.getResource("/testDataFile.fits.fz");
        URL u = ZFitsReaderTests.class.getResource("/testDataFileZSHRINK.fits.fz");

        FITS f = new FITS(u);
        HDU events = f.getHDU("Events").orElseThrow(() -> new RuntimeException("File did not contain HDU 'Events'"));
        BinTable binTable = events.getBinTable();
        ZFITSHeapReader heapReader = ZFITSHeapReader.forTable(binTable);

        int num = 0;
        for (OptionalTypesMap p : heapReader) {
            assertTrue(p.containsKey("Data"));
            assertEquals(p.size(), 9);
            num++;
        }
        assertTrue(num == 6);
    }

    @Test
    public void testZTileLen() throws Exception {
        //URL u =  CompareOldAndNewReaders.class.getResource("/testDataFile.fits.fz");
        URL u = ZFitsReaderTests.class.getResource("/testDataFileZTILELEN.fits.fz");

        FITS f = new FITS(u);
        HDU events = f.getHDU("Events").orElseThrow(() -> new RuntimeException("File did not contain HDU 'Events'"));
        BinTable binTable = events.getBinTable();
        ZFITSHeapReader heapReader = ZFITSHeapReader.forTable(binTable);

        int num = 0;
        for (OptionalTypesMap p : heapReader) {
            assertTrue(p.containsKey("Data"));
            assertEquals(p.size(), 11);
            num++;
        }
        assertTrue(num == 200);
    }

    @Test
    public void testEvenNumbers() throws Exception {
        URL u = ZFitsReaderTests.class.getResource("/testDataFile.fits.fz");

        FITS f = new FITS(u);
        HDU events = f.getHDU("Events").orElseThrow(() -> new RuntimeException("File did not contain HDU 'Events'"));
        BinTable binTable = events.getBinTable();
        ZFITSHeapReader heapReader = ZFITSHeapReader.forTable(binTable);
        for (int i = 1; i < 5; i++) {
            OptionalTypesMap<String, Serializable> row = heapReader.getNextRow();
            int eventNum = row.getInt("EventNum").orElse(0);
            assertEquals(i, eventNum);
        }
    }

}
