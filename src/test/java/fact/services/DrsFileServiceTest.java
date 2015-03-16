package fact.services;

import fact.auxservice.DrsFileService;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by kaibrugge on 13.11.14.
 */
public class DrsFileServiceTest {

    /**
     * Test what happens when there are no numbers in the filename
     * @throws Exception
     */
    @Test(expected = NumberFormatException.class)
    public void invalidFileNameTest() throws Exception{
        URL u = DrsFileService.class.getResource("/dummy_files/20140920_66.drs.fits");
        File dummyFile = new File(u.toURI());
        DrsFileService s = new DrsFileService();
        int filenumber = s.getFileNumberFromFile(dummyFile);
    }

    /**
     * Test what happens when the filename is too short.
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void wrongLengthNameTest() throws Exception{
        URL u = DrsFileService.class.getResource("/dummy_files/empty.drs.fits");
        File dummyFile = new File(u.toURI());
        DrsFileService s = new DrsFileService();
        int filenumber = s.getFileNumberFromFile(dummyFile);
    }

    @Test
    public void testDateAndNumberFromFile()throws Exception{
        URL u = DrsFileService.class.getResource("/dummy_files/20140920_012.drs.fits");
        File dummyFile = new File(u.toURI());
        DrsFileService s = new DrsFileService();
        int filenumber = s.getFileNumberFromFile(dummyFile);
        int datenumber = s.getDateNumberFromFile(dummyFile);
    }

    @Test
    public void testGetValidfiles() throws Exception{
        URL uDir = DrsFileService.class.getResource("/dummy_files/");
        URL uFile = DrsFileService.class.getResource("/dummy_files/20140920_012.drs.fits");
        File dummyDir = new File(uDir.toURI());
        File dummyFile = new File(uFile.toURI());
        DrsFileService s = new DrsFileService();
        int datenumber = s.getDateNumberFromFile(dummyFile);
        boolean valid = s.isDrsFileBelowFileNumber(dummyDir, dummyFile.getName(), datenumber, 022);
        assertTrue(valid);

        valid = s.isDrsFileBelowFileNumber(dummyDir, dummyFile.getName(), datenumber, 002);
        assertFalse(valid);
    }


}
