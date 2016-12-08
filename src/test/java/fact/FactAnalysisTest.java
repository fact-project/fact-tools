package fact;

import fact.io.FitsStreamTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

import static org.junit.Assert.fail;


public class FactAnalysisTest {



	public void factExampleXML() {

		try {
			URL url = FactAnalysisTest.class.getResource("/fact_example.xml");
			URL drsUrl =  FitsStreamTest.class.getResource("/testDrsFile.drs.fits.gz");
			URL dataUrl =  FitsStreamTest.class.getResource("/testDataFile.fits.gz");
			URL driveURL = FitsStreamTest.class.getResource("/testDriveFile.fits");
			URL integralGainsUrl = FitsStreamTest.class.getResource("/defaultIntegralGains.csv");
			URL outFile = FitsStreamTest.class.getResource("/outFileAscii.txt");
			String[] args = {url.toString(), "-Ddata="+dataUrl.toString(), "-DdataDRS="+drsUrl.toString(), "-Ddrive_file="+driveURL.toString(),"-DintegralGainsFile="+integralGainsUrl.toString(), "-Doutfile="+outFile.toString()};
			stream.run.main(args);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not run the ./fact_example.xml");
		}
	}
}

