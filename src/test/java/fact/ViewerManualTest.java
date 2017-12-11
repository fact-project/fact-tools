package fact;

import java.net.URL;

import static org.junit.Assert.fail;

/**
 * Created by kaibrugge on 20.03.14.
 */
public class ViewerManualTest {

    public static void main(String[] args){
        viewerXML();
    }

    //this cant be called during an automated unitest cause it needs some user input to exit
    public static void viewerXML() {

        try {
            URL url = ViewerManualTest.class.getResource("/viewertest.xml");
            URL drsUrl =  ViewerManualTest.class.getResource("/testDrsFile.drs.fits.gz");
            URL dataUrl =  ViewerManualTest.class.getResource("/testDataFile.fits.gz");
            URL driveUrl =  ViewerManualTest.class.getResource("/testDriveFile.fits");
            String[] args = {url.toString(), "-Dinput="+dataUrl.toString(), "-DdrsInput="+drsUrl.toString(), "-DdriveFile="+driveUrl.toString()};
            stream.run.main(args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            fail("Could not run the ./viewertest.xml");
        }
    }
}