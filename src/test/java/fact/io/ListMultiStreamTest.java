package fact.io;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.io.SourceURL;

import java.io.File;
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

//    @Test
//    public void readBrokenJsonTest() throws Exception {
//        URL u =  ListMultiStreamTest.class.getResource("/dummy_files/file_drs_list.json");
//        FactFileListMultiStream multiStream = new FactFileListMultiStream();
//        multiStream.setListUrl(new SourceURL(u));
//
//        try {
//            multiStream.setDrsPathKey("bla");
//            multiStream.init();
//        } catch (IllegalArgumentException e){
//            return;
//        }
//
//        fail("This should have thrown an IllegalArgumentException");
//    }

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

        FitsStream m = new FitsStream();
        multiStream.addStream("test", m);

        multiStream.init();

        Data data = multiStream.readNext();
        File drsFile = (File) data.get("@drsFile");

        assertThat(drsFile.getName(), is("20140920_66.drs.fits"));
    }
}
