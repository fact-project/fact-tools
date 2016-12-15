package fact.services;

import fact.DrsFileService;
import fact.auxservice.AuxFileService;
import fact.auxservice.AuxiliaryServiceName;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;
import org.junit.Test;
import stream.Data;
import stream.io.SourceURL;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

/**
 * Find the right folder and drsfile in the dummy raw files test resources.
 * The correct file for the testdatafile is 20130102_040.drs.fits.gz
 *
 * Created by kaibrugge on 17.11.14.
 */
public class DrsServiceTest {

    @Test
    public void testDrsFileFinder() throws Exception {
        URL u = DrsServiceTest.class.getResource("/dummy_files/raw/");

        DrsFileService s = new DrsFileService();
        s.setRawDataFolder(new SourceURL(u));

        u =  DrsServiceTest.class.getResource("/testDataFile.fits.gz");
        FitsStream stream = new FitsStream(new SourceURL(u));
        stream.init();
        Data item = stream.read();

        DrsFileService.CalibrationInfo info = s.getCalibrationConstantsForDataItem(item);

        assertTrue(info != null);
        assertTrue(info.timeOfCalibration.isEqual(LocalDateTime.parse("2013-01-02T21:59:00")));
    }

}
