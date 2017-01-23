package fact;

import fact.io.FITSStreamTest;

import java.net.URL;

import static org.junit.Assert.fail;


public class FactAnalysisTest {



	public void factExampleXML() {

		try {
			URL url = FactAnalysisTest.class.getResource("/fact_example.xml");
			URL drsUrl =  FITSStreamTest.class.getResource("/testDrsFile.drs.fits.gz");
			URL dataUrl =  FITSStreamTest.class.getResource("/testDataFile.fits.gz");
			URL driveURL = FITSStreamTest.class.getResource("/testDriveFile.fits");
			URL integralGainsUrl = FITSStreamTest.class.getResource("/defaultIntegralGains.csv");
			URL outFile = FITSStreamTest.class.getResource("/outFileAscii.txt");
			String[] args = {url.toString(), "-Ddata="+dataUrl.toString(), "-DdataDRS="+drsUrl.toString(), "-Ddrive_file="+driveURL.toString(),"-DintegralGainsFile="+integralGainsUrl.toString(), "-Doutfile="+outFile.toString()};
			stream.run.main(args);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not run the ./fact_example.xml");
		}
	}
}

