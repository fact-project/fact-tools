package fact.io.hdureader;

import org.junit.Test;

import java.io.Serializable;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the basics of the zfitsheapreader
 *
 * Created by mackaiver on 14/12/16.
 */
public class ZFitsReaderTests {

    @Test
    public void testHeapIterator() throws Exception {
        //URL u =  CompareOldAndNewReaders.class.getResource("/testDataFile.fits.fz");
        URL u =  ZFitsReaderTests.class.getResource("/testDataFile.fits.fz");

        FITS f = new FITS(u);
        HDU events = f.getHDU("Events");
        BinTable binTable = events.getBinTable();
        ZFITSHeapReader heapReader = ZFITSHeapReader.forTable(binTable);

        for(OptionalTypesMap p : heapReader){
            assertTrue(p.containsKey("Data"));
            assertTrue(p.size() == 9);
        }
    }

    @Test
    public void testEvenNumbers() throws Exception {
        URL u =  ZFitsReaderTests.class.getResource("/testDataFile.fits.fz");

        FITS f = new FITS(u);
        HDU events = f.getHDU("Events");
        BinTable binTable = events.getBinTable();
        ZFITSHeapReader heapReader = ZFITSHeapReader.forTable(binTable);
        for (int i = 1; i < 5; i++) {
            OptionalTypesMap<String, Serializable> row = heapReader.getNextRow();
            int eventNum = row.getInt("EventNum").orElse(0);
            assertEquals(i, eventNum);
        }
    }

}
