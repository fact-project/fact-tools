package fact.services;

import fact.auxservice.AuxiliaryServiceName;
import fact.auxservice.AuxFileService;
import org.junit.Test;
import stream.io.SourceURL;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

/**
 * Created by kaibrugge on 17.11.14.
 */
public class AuxServiceTest {


    /**
     * Tests what happens when the folder to the datestring exists but the files in that folder
     * don't have the right datestring. We expect an empty map in response.
     * @throws Exception
     */
    @Test
    public void testWrongFilename() throws Exception {
        URL u = AuxServiceTest.class.getResource("/dummy_files/aux/2015/09/");
        AuxFileService s = new AuxFileService();
        //supply a wrong datestring
//        String dateString = "20150921";
        HashMap<AuxiliaryServiceName, SourceURL> m =  s.findAuxFileUrls(new SourceURL(u));
        assertTrue(m.isEmpty());
    }


    @Test
    public void testAuxFileFinder() throws Exception {
        URL u = AuxServiceTest.class.getResource("/dummy_files/aux/2015/09/21");
//        SourceURL url = new SourceURL(u);
        AuxFileService s = new AuxFileService();
        //supply datestring. ITS MY BIRFDAY! YAY
//        String dateString = "20150920";
        HashMap<AuxiliaryServiceName, SourceURL> m =  s.findAuxFileUrls(new SourceURL(u));
        assertTrue(m.containsKey(AuxiliaryServiceName.DRIVE_CONTROL_TRACKING_POSITION));
//        assertTrue(m.containsKey("DRIVE_CONTROL_POINTING_POSITION"));
    }

}
