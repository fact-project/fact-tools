package fact.services;

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
     * We expect a FileNotFoundException when the folder to the datestring does not exist
     * @throws Exception
     */
    @Test(expected = FileNotFoundException.class)
    public void testWrongFolder() throws Exception {
        URL u = AuxServiceTest.class.getResource("/dummy_files/aux/");
        SourceURL url = new SourceURL(u);
        AuxFileService s = new AuxFileService();
        //supply a wrong datestring
        String dateString = "21092014";
        HashMap<String, SourceURL> m =  s.findAuxFileUrls(new SourceURL(u), dateString);
    }

    /**
     * Tests what happens when the folder to the datestring exists but the files in that folder
     * don't have the right datestring. We expect an empty map in response.
     * @throws Exception
     */
    @Test
    public void testWrongFilename() throws Exception {
        URL u = AuxServiceTest.class.getResource("/dummy_files/aux/");
        SourceURL url = new SourceURL(u);
        AuxFileService s = new AuxFileService();
        //supply a wrong datestring
        String dateString = "20150921";
        HashMap<String, SourceURL> m =  s.findAuxFileUrls(new SourceURL(u), dateString);
        assertTrue(m.isEmpty());
    }


    @Test
    public void testAuxFileFinder() throws Exception {
        URL u = AuxServiceTest.class.getResource("/dummy_files/aux/");
        SourceURL url = new SourceURL(u);
        AuxFileService s = new AuxFileService();
        //supply datestring. ITS MY BIRFDAY! YAY
        String dateString = "20150920";
        HashMap<String, SourceURL> m =  s.findAuxFileUrls(new SourceURL(u), dateString);
        assertTrue(m.containsKey("DRIVE_CONTROL_TRACKING_POSITION"));
        assertTrue(m.containsKey("DRIVE_CONTROL_POINTING_POSITION"));
    }

}
