package fact.io;

import com.google.common.io.ByteStreams;
import fact.io.hdureader.BinTable;
import fact.io.hdureader.Fits;
import fact.io.hdureader.HDU;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;

import java.util.zip.GZIPInputStream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


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
//
//
//        ZFitsStream stream = new ZFitsStream(new SourceURL(u));
//        stream.tableName = "Events";
//        stream.init();
//
//        Data item = stream.read();
//        System.out.println(b);
////        int read = inputStreamForHDUData.read();
////        System.out.println(read);
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

        BinTable b = new BinTable(events, inputStreamForHDUData);
////        int read = inputStreamForHDUData.read();
////        System.out.println(read);
    }


    @Test
    public void testFitsBinTable() throws Exception {
        URL u =  FitsStreamTest.class.getResource("/testDataFile.fits.gz");

        Fits f = new Fits(u);

        HDU events = f.getHDU("Events").orElseThrow(IOException::new);

        DataInputStream stream = f.getInputStreamForHDUData(events);

        BinTable b = new BinTable(events, stream);
        assertThat(b.numberOfRowsInTable, is(10749));

////        int read = inputStreamForHDUData.read();
////        System.out.println(read);
    }
}
