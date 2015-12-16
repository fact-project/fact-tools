package fact.io;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.LoggerFactory;
import stream.io.SourceURL;

import java.io.IOException;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * This tests creates a dummy file tree in a temporyry folder. It will then run against a number of glob patterns
 * Created by kai on 16.12.15.
 */
public class RecursiveDirectoryStreamTest {
    static org.slf4j.Logger log = LoggerFactory.getLogger(RecursiveDirectoryStreamTest.class);

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    private void createTestFiles() throws IOException {
        folder.newFolder("aux", "2013", "08");
        folder.newFolder("aux", "2013", "09");
        folder.newFolder("aux", "2014", "09");
        folder.newFile("aux/2013/08/20150812.DRIVE_CONTROL_SOURCE_POSITION.fits");
        folder.newFile("aux/2013/09/20150812.DRIVE_CONTROL_TRACKING_POSITION.fits");
        folder.newFile("aux/2013/09/20150812.DRIVE_CONTROL_POINTING_POSITION.fits");
        folder.newFile("aux/2014/09/20150812.DRIVE_CONTROL_SOURCE_POSITION.fits");
        folder.newFile("aux/2014/09/20150812.DRIVE_CONTROL_POINTING_POSITION.fits");
        folder.newFile("aux/2014/09/20150812.DRIVE_CONTROL_TRACKING_POSITION.fits");
    }

    @Test
    public void testGlob() throws Exception {

        createTestFiles();
        SourceURL sourceUrl = new SourceURL("file://"+ folder.getRoot());

        RecursiveDirectoryStream r = new RecursiveDirectoryStream(sourceUrl);

        String pattern = "/**/*DRIVE_CONTROL_{TRACKING,POINTING}_POSITION.fits";
        r.setPattern(pattern);
        r.init();

        log.info("Found files: {}", r.files);
        assertThat(r.files.size(), is(4));
    }

    @Test
    public void testGlobTrackingSourcePos() throws Exception {

        createTestFiles();
        SourceURL sourceUrl = new SourceURL("file://"+ folder.getRoot());
        RecursiveDirectoryStream r = new RecursiveDirectoryStream(sourceUrl);


        String pattern = "/**/*DRIVE_CONTROL_{TRACKING,SOURCE}_POSITION.fits";
        r.setPattern(pattern);
        r.init();

        log.info("Found files: {}", r.files);
        assertThat(r.files.size(), is(4));
    }

    @Test
    public void testGlobSourcePos() throws Exception {

        createTestFiles();
        SourceURL sourceUrl = new SourceURL("file://"+ folder.getRoot());
        RecursiveDirectoryStream r = new RecursiveDirectoryStream(sourceUrl);


        String pattern = "/**/*DRIVE_CONTROL_SOURCE_POSITION.fits";
        r.setPattern(pattern);
        r.init();

        log.info("Found files: {}", r.files);
        assertThat(r.files.size(), is(2));
    }

    @Test
    public void testGlobPos() throws Exception {

        createTestFiles();
        SourceURL sourceUrl = new SourceURL("file://"+ folder.getRoot());
        RecursiveDirectoryStream r = new RecursiveDirectoryStream(sourceUrl);

        String pattern = "/*DRIVE_CONTROL_SOURCE_POSITION.fits";
        r.setPattern(pattern);
        r.init();

        log.info("Found files: {}", r.files);
        assertThat(r.files.size(), is(0));
    }

    @Test
    public void testInvalidGlobPattern() throws Exception {

        createTestFiles();
        SourceURL sourceUrl = new SourceURL("file://"+ folder.getRoot());
        RecursiveDirectoryStream r = new RecursiveDirectoryStream(sourceUrl);

        String pattern = "/*DRIVE<>?!§$%&/%_SOURCE_POSITION.fits";
        r.setPattern(pattern);
        r.init();

        log.info("Found files: {}", r.files);
        assertThat(r.files.size(), is(0));
    }


}
