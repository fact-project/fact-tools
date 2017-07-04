package fact.io;

import org.junit.Test;
import stream.Data;
import stream.io.SourceURL;

import java.net.URL;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Kai on 27.06.17.
 */
public class CeresStreamTest {

    @Test
    public void testFitsStream() {

        try {
            URL u =  FITSStreamTest.class.getResource("/ceres_output/18000/00018000.000_D_MonteCarlo018_Events.fits.gz");
            SourceURL url = new SourceURL(u);

            CeresStream stream = new CeresStream(url);
            stream.init();

            Data item = stream.read();
            while (item != null) {
                item = stream.read();
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Could not read FitsFile");
        }
    }

    @Test
    public void testRunHeaderKeys() {

        try {
            URL u =  FITSStreamTest.class.getResource("/ceres_output/18000/00018000.000_D_MonteCarlo018_Events.fits.gz");
            SourceURL url = new SourceURL(u);

            CeresStream stream = new CeresStream(url);
            stream.init();

            Data item = stream.read();
            while (item != null) {
                assertTrue(item.get("MCorsikaRunHeader.fParticleID") != null);
                assertTrue(item.get("MCorsikaRunHeader.fNumReuse") != null);
                assertTrue(item.get("MCorsikaRunHeader.fImpactMax") != null);
                assertTrue(item.get("MCorsikaRunHeader.fRunNumber") != null);
                item = stream.read();
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Could not read FitsFile");
        }
    }
}
