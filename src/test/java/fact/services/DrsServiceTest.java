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
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

/**
 * Created by kaibrugge on 17.11.14.
 */
public class DrsServiceTest {


    /**
     * Tests what happens when the folder to the datestring exists but the files in that folder
     * don't have the right datestring. We expect an empty map in response.
     * @throws Exception
     */
//    @Test
//    public void testWrongFilename() throws Exception {
//        URL u = DrsServiceTest.class.getResource("/dummy_files/aux/2015/09/");
//        AuxFileService s = new AuxFileService();
//        //supply a wrong datestring
////        String dateString = "20150921";
//        HashMap<AuxiliaryServiceName, SourceURL> m =  s.findAuxFileUrls(new SourceURL(u));
//        assertTrue(m.isEmpty());
//    }


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

//        assertTrue(m.containsKey("DRIVE_CONTROL_POINTING_POSITION"));
    }

}
