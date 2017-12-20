package fact.io;

import fact.io.hdureader.FITSStream;
import org.junit.Test;
import stream.Data;
import stream.io.SourceURL;

import java.io.File;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by kaibrugge on 14.04.15.
 */
public class ListMultiStreamTest {

    @Test
    public void readJsonTest() throws Exception {
        URL u = ListMultiStreamTest.class.getResource("/dummy_files/file_drs_list.json");
        FactFileListMultiStream multiStream = new FactFileListMultiStream(new SourceURL(u));
        multiStream.setUrl(new SourceURL(u));

        assertThat(multiStream.fileQueue.size(), is(0));
        multiStream.init();
        assertThat(multiStream.fileQueue.size(), is(1));
    }

    @Test
    public void testDrsInjection() throws Exception {
        URL u = FITSStreamTest.class.getResource("/dummy_files/file_drs_list.json");
        FactFileListMultiStream multiStream = new FactFileListMultiStream(new SourceURL(u));
        multiStream.setUrl(new SourceURL(u));

        FITSStream m = new FITSStream();
        multiStream.addStream("test", m);

        multiStream.init();

        Data data = multiStream.readNext();
        File drsFile = (File) data.get("@drsFile");

        assertThat(drsFile.getName(), is("20140920_66.drs.fits"));
    }
}
