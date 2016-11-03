package fact.io;

import com.google.common.io.ByteStreams;
import fact.io.hdureader.Fits;
import fact.io.hdureader.HDU;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;

import java.util.zip.GZIPInputStream;


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
    public void testFits() throws IOException {
        URL u =  FitsStreamTest.class.getResource("/testDataFile.fits.fz");

        Fits f = new Fits(u);

        HDU events = f.getHDU("Events").orElseThrow(IOException::new);
        HDU zDrsCellOffsets = f.getHDU("ZDrsCellOffsets").orElseThrow(IOException::new);

        InputStream inputStreamForHDUData = f.getInputStreamForHDUData(zDrsCellOffsets);
        int b = new DataInputStream(inputStreamForHDUData).read();
        System.out.println(b);
////        int read = inputStreamForHDUData.read();
////        System.out.println(read);
    }
}
