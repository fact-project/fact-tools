package fact.rta.io;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import stream.io.SourceURL;

import java.io.File;
import java.net.URL;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.*;
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
        String jdbc = "jdbc:sqlite:"+ dbFile.getCanonicalPath();
        SourceURL auxFolder = new SourceURL(RTAStreamTest.class.getResource("/dummy_files/aux/"));


        RTAStream rtaStream = new RTAStream();
        rtaStream.folder = resource.getPath();
        rtaStream.auxFolder = auxFolder;
        rtaStream.jdbcConnection = jdbc;
        rtaStream.init();

        for (int i = 0; i < 6; i++) {
            Path path = rtaStream.fileQueue.poll(10, TimeUnit.SECONDS);
            assertThat(path, is(not(nullValue())));
        }
    }

    @Test
    public void testFileWalker() throws Exception {
        URL resource = RTAStreamTest.class.getResource("/./");
        Path rootPath = Paths.get(resource.toURI());
        PathMatcher regex = FileSystems.getDefault().getPathMatcher("regex:\\d{8}_\\d{3}.(fits|zfits)(.gz|.fz)?");
        assertThat(regex.matches(Paths.get("20130101_012.fits.gz")), is(true));

        List<Path> paths = Files.walk(rootPath)
                .filter(path -> Files.isRegularFile(path))
                .filter(Files::isReadable)
                .map(Path::getFileName)
                .filter(regex::matches)
                .collect(toList());

        //there are 5 valid test files at the moment
        assertThat(paths.size(), is(6));
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