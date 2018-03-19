package fact.io.hdureader;

import org.junit.Test;

import stream.io.SourceURL;
import stream.Data;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Test the FITS object
 * Created by mackaiver on 14/12/16.
 */
public class FitsHDUTests {

    @Test
    public void testToOpenInputStream() throws Exception {
        URL u = FitsHDUTests.class.getResource("/testDataFile.fits.fz");

        FITS f = new FITS(u);

        HDU events = f.getHDU("Events").orElseThrow(() -> new RuntimeException("File did not contain HDU 'Events'"));

        assertThat(events.header.get("EXTNAME").orElse("WRONG"), is("Events"));

        HDU zDrsCellOffsets = f.getHDU("ZDrsCellOffsets").orElseThrow(() -> new RuntimeException("File did not contain HDU 'ZDrsCellOffsets'"));

        f.getInputStreamForHDUData(zDrsCellOffsets);

    }

    @Test
    public void testBinTableReaderSkip() throws Exception {
        URL u = FitsHDUTests.class.getResource("/testDataFile.fits.gz");
        FITSStream fits = new FITSStream(new SourceURL(u));
        fits.init();
        FITSStream fits2 = new FITSStream(new SourceURL(u));
        fits2.init();

        //read 3
        fits.readNext();
        fits.readNext();
        Data item = fits.readNext();
        fits2.skipRows(2);
        Data item2 = fits2.readNext();


        int eNr = (int) item.get("EventNum");
        int eNr2 = (int) item2.get("EventNum");
        assertEquals(eNr, eNr2);

        short[] data = (short[]) item.get("Data");
        short[] data2 = (short[]) item2.get("Data");
        assertArrayEquals(data, data2);
    }

    @Test
    public void testZFitsReaderSkip() throws Exception {
        for (int i = 2; i < 5; i++) {
            System.out.println("Test skip: " + i);
            URL u = FitsHDUTests.class.getResource("/testDataFile.fits.fz");
            FITSStream fits = new FITSStream(new SourceURL(u));
            fits.init();
            FITSStream fits2 = new FITSStream(new SourceURL(u));
            fits2.init();

            for (int j = 0; j < i; j++) {
                fits.readNext();
            }
            Data item = fits.readNext();

            fits2.skipRows(i);
            Data item2 = fits2.readNext();
            
            int eNr = (int) item.get("EventNum");
            int eNr2 = (int) item2.get("EventNum");
            assertEquals(eNr, eNr2);

            short[] data = (short[]) item.get("Data");
            short[] data2 = (short[]) item2.get("Data");
            assertArrayEquals(data, data2);
        }
    }

    @Test
    public void testZFitsReaderSkipWithZShrink() throws Exception {
        for (int i = 0; i < 5; i++) {
            System.out.println("Test skip: " + i);
            URL u = FitsHDUTests.class.getResource("/testDataFileZSHRINK.fits.fz");
            FITSStream fits = new FITSStream(new SourceURL(u));
            fits.init();
            FITSStream fits2 = new FITSStream(new SourceURL(u));
            fits2.init();

            for (int j = 0; j < i; j++) {
                fits.readNext();
            }
            Data item = fits.readNext();

            fits2.skipRows(i);
            Data item2 = fits2.readNext();


            int eNr = (int) item.get("EventNum");
            int eNr2 = (int) item2.get("EventNum");
            assertEquals(eNr, eNr2);

            short[] data = (short[]) item.get("Data");
            short[] data2 = (short[]) item2.get("Data");
            assertArrayEquals(data, data2);
        }
    }

    @Test
    public void testZFitsReaderSkipWithZTileLen() throws Exception {
        for (int i = 98; i < 102; i++) {
            System.out.println("Test skip: " + i);
            URL u = FitsHDUTests.class.getResource("/testDataFileZTILELEN.fits.fz");
            FITSStream fits = new FITSStream(new SourceURL(u));
            fits.init();
            FITSStream fits2 = new FITSStream(new SourceURL(u));
            fits2.init();

            for (int j = 0; j < i; j++) {
                fits.readNext();
            }
            Data item = fits.readNext();

            fits2.skipRows(i);
            Data item2 = fits2.readNext();


            int eNr = (int) item.get("EventNum");
            int eNr2 = (int) item2.get("EventNum");
            assertEquals(eNr, eNr2);

            short[] data = (short[]) item.get("Data");
            short[] data2 = (short[]) item2.get("Data");
            assertArrayEquals(data, data2);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testGzipCheck() throws IOException {
        URL u = FitsHDUTests.class.getResource("/testDataFile.fits.gz");

        byte[] header = new byte[2];
        u.openStream().read(header);

        assertTrue(FITS.isGzippedCompressed(header));


        u = FitsHDUTests.class.getResource("/testDataFile.fits.fz");

        header = new byte[2];
        u.openStream().read(header);

        assertFalse(FITS.isGzippedCompressed(header));
    }

    @Test
    public void testInputStreamFromFitsFile() throws Exception {
        URL u = FitsHDUTests.class.getResource("/testDataFile.fits.gz");

        FITS f = new FITS(u);

        HDU events = f.getHDU("Events").orElseThrow(() -> new RuntimeException("File did not contain HDU 'Events'"));

        DataInputStream inputStreamForHDUData = f.getInputStreamForHDUData(events);

        int eventNum = inputStreamForHDUData.readInt();

        assertThat(eventNum, is(1));
    }

    @Test
    public void testPrimaryHDU() throws Exception {
        URL u = FitsHDUTests.class.getResource("/testDataFile.fits.gz");

        FITS f = new FITS(u);

        HDU primaryHDU = f.primaryHDU;

        assertTrue(primaryHDU.isPrimaryHDU);

        String checkSum = primaryHDU.header.get("CHECKSUM").orElse("Wrong");

        assertThat(checkSum, is("4AcB48bA4AbA45bA"));
    }
}
