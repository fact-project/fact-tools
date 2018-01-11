package fact.io.hdureader;

import org.junit.Test;

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
