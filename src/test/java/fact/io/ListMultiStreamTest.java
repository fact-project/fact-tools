package fact.io;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.io.SourceURL;

import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by kaibrugge on 14.04.15.
 */
public class ListMultiStreamTest {
    static Logger log = LoggerFactory.getLogger(ListMultiStreamTest.class);

    @Test
    public void readBrokenJsonTest() throws Exception {
        URL u =  ListMultiStreamTest.class.getResource("/dummy_files/file_drs_list.json");
        FactFileListMultiStream multiStream = new FactFileListMultiStream();
        multiStream.setListUrl(new SourceURL(u));

        try {
            multiStream.setDrsPathKey("bla");
            multiStream.init();
        } catch (IllegalArgumentException e){
            return;
        }

        fail("This shoudl have thrown an illegalargumentexception");
    }

    @Test
    public void readJsonTest() throws Exception {
        URL u =  ListMultiStreamTest.class.getResource("/dummy_files/file_drs_list.json");
        FactFileListMultiStream multiStream = new FactFileListMultiStream();
        multiStream.setListUrl(new SourceURL(u));

        assertThat(multiStream.fileQueue.size(), is(0));
        multiStream.init();
        assertThat(multiStream.fileQueue.size(), is(1));
    }

    @Test
    public void testDrsInjection() throws Exception {
        FactFileListMultiStream multiStream = new FactFileListMultiStream();
        URL u =  FitsStreamTest.class.getResource("/dummy_files/file_drs_list.json");
        multiStream.setListUrl(new SourceURL(u));
        multiStream.init();

        FactFileListMultiStream.DataDrsPair p = multiStream.fileQueue.poll();
        FitsStream m = new FitsStream();
        multiStream.setStreamProperties(m, p);
        assertThat(m.drsFile, is(notNullValue()));
    }
}
