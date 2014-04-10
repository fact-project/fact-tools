package fact.viewer.ui;

import fact.io.FitsStreamTest;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.fail;

/**
 * Created by kaibrugge on 20.03.14.
 */
public class ViewerManualTest {

    public static void main(String[] args){
        drsCalibXML();
    }

    //this cant be called during an automated unitest cause it needs some user input to exit
    public static void drsCalibXML() {

        try {
            URL url = ViewerManualTest.class.getResource("/viewertest.xml");
            URL drsUrl =  ViewerManualTest.class.getResource("/test.drs.fits.gz");
            URL dataUrl =  ViewerManualTest.class.getResource("/sample.fits.gz");
            String[] args = {url.toString(), "-Dinput="+dataUrl.toString(), "-DdrsInput="+drsUrl.toString()};
            stream.run.main(args);
        } catch (Exception e) {
            fail("Could not run the ./drsTest.xml");
            e.printStackTrace();
        }
    }
}
