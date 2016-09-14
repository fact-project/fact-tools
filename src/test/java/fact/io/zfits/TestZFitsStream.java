package fact.io.zfits;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.io.SourceURL;

import java.net.URL;

public class TestZFitsStream {


    /**
     * Test whether gzipped fits files containing raw data can be read correctly
     * @throws Exception
     */
	@Test
	public void testReadFits() throws Exception {
		URL u =  TestZFitsStream.class.getResource("/testDataFile.fits.gz");
		ZFitsStream stream = new ZFitsStream(new SourceURL(u));
		stream.tableName = "Events";
		stream.init();
		
		Data item = stream.read();
		int i = 1;
        //the test file contains just 15 valid events.
		while (i < 15) {
			item = stream.read();
            i++;
		}
	}
    /**
     * Test whether gzippe fits files containing MC data can be read correctly
     * @throws Exception
     */
    @Test
    public void testReadMCFits() throws Exception {
        URL u =  TestZFitsStream.class.getResource("/testMcFile.fits.gz");
        ZFitsStream stream = new ZFitsStream(new SourceURL(u));
        stream.tableName = "Events";
        stream.init();

        Data item = stream.read();
        printItemsInStream(stream, item);
    }

    /**
     * Test  gzipped fits files containing DRS constants for real data
     * @throws Exception
     */
    @Test
    public void testReadDRSFits() throws Exception {
        URL u =  TestZFitsStream.class.getResource("/testDrsFile.drs.fits.gz");
        ZFitsStream stream = new ZFitsStream(new SourceURL(u));
        stream.tableName = "DrsCalibration";
        stream.init();

        Data item = stream.read();
        printItemsInStream(stream, item);
    }

    /**
     * Test  gzipped fits files containing DRS constants for real data
     * @throws Exception
     */
    @Test
    public void testReadMCDRSFits() throws Exception {
        URL u =  TestZFitsStream.class.getResource("/testMcDrsFile.drs.fits.gz");
        ZFitsStream stream = new ZFitsStream(new SourceURL(u));
        stream.tableName = "DrsCalibration";
        stream.init();

        Data item = stream.read();
        printItemsInStream(stream, item);
    }


    /**
     * Test whether drive file in fits format can be read correctly
     * @throws Exception
     */
    @Test
    public void testDriveFits() throws Exception {
        URL u =  TestZFitsStream.class.getResource("/testDriveFile.fits");
        ZFitsStream stream = new ZFitsStream(new SourceURL(u));
        stream.tableName = "DRIVE_CONTROL_TRACKING_POSITION";
        stream.init();

        Data item = stream.read();
        printItemsInStream(stream, item);
    }

    /**
     * simply loop over items in the stream and print some information about them
     */
    private void printItemsInStream(ZFitsStream stream, Data item) throws Exception {
        int i = 1;
        while (item != null) {
            item = stream.read();
            i++;
        }
    }
}
