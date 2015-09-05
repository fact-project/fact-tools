package fact.datacorrection;

import fact.io.FitsStream;
import fact.io.FitsStreamTest;
import org.junit.Before;
import org.junit.Test;
import stream.Data;
import stream.io.SourceURL;

import java.io.File;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by kaibrugge on 27.04.15.
 */
public class DrsCalibrationTest {

    URL drsUrl =  DrsCalibrationTest.class.getResource("/testDrsFile.drs.fits.gz");
    URL dataUrl =  FitsStreamTest.class.getResource("/testDataFile.fits.gz");

    private FitsStream stream;

    @Before
    public void setup() throws Exception {
        stream = new FitsStream(new SourceURL(dataUrl));
        stream.init();
    }

    @Test
    public void testMissingURLParameter() throws Exception{
        DrsCalibration pr = new DrsCalibration();
        pr.setOutputKey("test");
        pr.init(null);

        Data item = stream.readNext();

        try {
            pr.process(item);
        } catch (IllegalArgumentException e){
            return;
        }
        fail("Expected an IllegalArgumentException.");
    }

    @Test
    public void testDrsURLinStream() throws Exception{

        DrsCalibration pr = new DrsCalibration();
        pr.setOutputKey("test");
        pr.init(null);

        Data item = stream.readNext();
        item.put("@drsFile", new File(drsUrl.getFile()));

        item = pr.process(item);
        assertThat(item.containsKey("test"), is(true));

    }

}
