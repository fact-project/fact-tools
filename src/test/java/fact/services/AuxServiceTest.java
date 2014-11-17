package fact.services;

import fact.auxservice.AuxFileService;
import fact.auxservice.DrsFileService;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import stream.io.SourceURL;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

/**
 * Created by kaibrugge on 17.11.14.
 */
public class AuxServiceTest {

    @Test(expected = FileNotFoundException.class)
    public void testWrongFolder() throws Exception {
        URL u = AuxServiceTest.class.getResource("/dummy_files/aux/");
        SourceURL url = new SourceURL(u);
        AuxFileService s = new AuxFileService();
        //supply a wrong datestring
        String dateString = "21092014";
        HashMap<String, SourceURL> m =  s.findAuxFileUrls(new SourceURL(u), dateString);
    }


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
