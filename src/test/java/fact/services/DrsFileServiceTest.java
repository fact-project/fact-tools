package fact.services;

import fact.DrsFileService;
import fact.io.hdureader.FITSStream;
import org.junit.Test;
import stream.Data;
import stream.io.SourceURL;

import java.net.URL;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertTrue;


/**
 * Find the right folder and drsfile in the dummy raw files test resources.
 * The correct file for the testdatafile is 20130102_040.drs.fits.gz
 *
 * Created by kaibrugge on 17.11.14.
 */
public class DrsFileServiceTest {

    @Test
    public void testDrsFileFinder() throws Exception {
        URL u = DrsFileServiceTest.class.getResource("/dummy_files/raw/");

        DrsFileService s = new DrsFileService();
        s.rawDataFolder = new SourceURL(u);

        u =  DrsFileServiceTest.class.getResource("/testDataFile.fits.gz");
        FITSStream stream = new FITSStream(new SourceURL(u));
        stream.init();
        Data item = stream.read();

        DrsFileService.CalibrationInfo info = s.getCalibrationConstantsForDataItem(item);
        assertTrue(info != null);
        assertTrue(info.timeOfCalibration.isEqual(ZonedDateTime.parse("2013-01-02T21:59:00+00:00")));
    }

}
