package fact.datacorrection;

import fact.run;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;

public class DrsCalibrationTests {


    @BeforeClass
    public static void setup() {
        try {
            URL.setURLStreamHandlerFactory(new run.ClasspathURLStreamHandlerFactory());
        } catch (Error e) {
            // ignore error that happens in multi threaded environments where handler has already been set
        }
    }

    @Test
    public void testURLParsing() throws Exception{
        DrsCalibration drsCalibration =  new DrsCalibration();

        String[] urls = {
                "file:src/test/resources/testDrsFile.drs.fits.gz",
                "classpath:/testDrsFile.drs.fits.gz",
        };

        for (String urlString: urls) {
            URL url = new URL(urlString);

            drsCalibration.url = urlString;
            drsCalibration.init(null);
            assertEquals(url, drsCalibration.drsFileURL);
        }

        String urlString = "src/test/resources/testDrsFile.drs.fits.gz";
        URL url = new URL("file:" + urlString);

        drsCalibration.url = urlString;
        drsCalibration.init(null);
        assertEquals(url, drsCalibration.drsFileURL);
    }
}
