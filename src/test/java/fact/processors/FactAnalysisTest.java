package fact.processors;

import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.io.SourceURL;

import fact.cleaning.CoreNeighborClean;
import fact.features.SourcePosition;
import fact.features.PhotonCharge;
import fact.features.MaxAmplitudePosition;
import fact.filter.DrsCalibration;
import fact.io.FitsStream;
import fact.io.FitsStreamTest;


public class FactAnalysisTest {

	static Logger log = LoggerFactory.getLogger(FactAnalysisTest.class);


	@Test
	public void factExampleXML() {

		try {
			URL url = FactAnalysisTest.class.getResource("/fact_example.xml");
			URL drsUrl =  FitsStreamTest.class.getResource("/test.drs.fits.gz");
			URL dataUrl =  FitsStreamTest.class.getResource("/sample.fits.gz");
			URL driveURL = FitsStreamTest.class.getResource("/drive_file.fits");
			URL outFile = FitsStreamTest.class.getResource("/outFileAscii.txt");
			String[] args = {url.toString(), "-Ddata="+dataUrl.toString(), "-DdataDRS="+drsUrl.toString(), "-Ddrive_file="+driveURL.toString(), "-Doutfile="+outFile.toString()};
			stream.run.main(args);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not run the ./fact_example.xml");
		}
	}

	@SuppressWarnings("unused")
	@Test
	public void testPreCalc() {

		try {
			URL drsUrl =  FitsStreamTest.class.getResource("/test.drs.fits.gz");
			DrsCalibration pr = new DrsCalibration();
			pr.setUrl(drsUrl.toString());
			pr.setOutputKey("test");

			MaxAmplitudePosition pP = new MaxAmplitudePosition();
			pP.setKey("test");
			pP.setOutputKey("positions");

			PhotonCharge c = new PhotonCharge();
			c.setKey("test");
			c.setOutputKey("photonCharge");

			CoreNeighborClean clean = new CoreNeighborClean();
			clean.setKey("photonCharge");
			clean.setOutputKey("shower");

			URL driveURL = FitsStreamTest.class.getResource("/drive_file.fits");
			SourcePosition poser = new SourcePosition();
			poser.setUrl(driveURL);
			poser.setPhysicalSource("crab");
			poser.setOutputKey("pos");
			poser.init(null);

			URL dataUrl =  FitsStreamTest.class.getResource("/sample.fits.gz");
			SourceURL url = new SourceURL(dataUrl);

			FitsStream stream = new FitsStream(url);
			stream.init();
			Data item = stream.read();
			while (item != null) {
				pr.process(item);
				if (!item.containsKey("test"))
					fail("Item does not contain the right key after drs calibration");
				try{
					double[] result = (double[]) item.get("test");
					short[] ar = (short[]) item.get("Data");
					if(ar.length != result.length){
						fail("drxCalibration is not working. the result array doesnt have the smae lenght as the original array");
					}

					pP.process(item);
					if (!item.containsKey("positions"))
						fail("Item does not contain the right key after maxamplitude positions");
					int[] pos = (int[]) item.get("positions");

					c.process(item);
					if (!item.containsKey("photonCharge"))
						fail("Item does not contain the right key after calcphotoncharge");
					double[] charge = (double[]) item.get("photonCharge");

					clean.process(item);
//					if (!item.containsKey("shower"))
//						fail("Item does not contain the right key after cleaning");
//					int[] s = (int[]) item.get("shower");

					poser.process(item);
					if (!item.containsKey("pos"))
						fail("Item does not contain the right key after calcsourcepos");
					double[] p = (double[]) item.get("pos");
					if(p.length != 2){
						fail("Calcsourcepost did not output the right array");
					}
				} catch(ClassCastException e){
					fail("Failed to cast items to the right types");
				}

				item = stream.read();
			}

		} catch(ClassCastException e){
			fail("Wrong datatypes.");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not execute drsCalibration");
		}
	}
}

