package fact.rta;

import fact.rta.io.RTAStream;
import nom.tam.fits.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by mackaiver on 21/09/16.
 */
public class RTAStreamTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testFileMatching() throws Exception {
        URL resource = RTAStreamTest.class.getResource("/./");
        File dbFile = temporaryFolder.newFile("test.sqlite");

        RTAStream rtaStream = new RTAStream();
        rtaStream.jdbcConnection = "jdbc:sqlite:"+ dbFile.getCanonicalPath();
        rtaStream.folder = temporaryFolder.newFolder("empty").getAbsolutePath();
        rtaStream.init();

        Files.walkFileTree(Paths.get(resource.toURI()), rtaStream.new RegexVisitor("\\d{8}_\\d{3}.(fits|zfits)(.gz)?"));
        //there are 3 valid test files at the moment
        assertThat(rtaStream.fileQueue.size(), is(3));
    }


    @Test
    public void testFileNameToRunID(){
        assertThat(RTAStream.filenameToRunID("20130101_012.fits.gz").orElse(0), is(12));
        assertThat(RTAStream.filenameToRunID("20130101_912.fits.gz").orElse(0), is(912));

        assertThat(RTAStream.filenameToRunID("20130101_91s2.fits.gz").orElse(0), is(0));
    }

    @Test
    public void testFileNameToFACTNight(){
        assertThat(RTAStream.filenameToFACTNight("20130101_012.fits.gz").orElse(0), is(20130101));
        assertThat(RTAStream.filenameToFACTNight("20130201_912.fits.gz").orElse(0), is(20130201));
        assertThat(RTAStream.filenameToFACTNight("19990101_91s2.fits.gz").orElse(0), is(19990101));

        assertThat(RTAStream.filenameToFACTNight("19asd990101_91s2.fits.gz").orElse(0), is(0));
    }


}
