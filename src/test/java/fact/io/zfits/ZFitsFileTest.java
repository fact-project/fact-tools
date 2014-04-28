package fact.io.zfits;

//import static org.junit.Assert.*;

import java.io.IOException;

import stream.util.parser.ParseException;

public class ZFitsFileTest {
	public void headerTest() throws IOException, ParseException {
		//String filename = ZFitsFileTest.class.getResource("/zfits_test.fz").getFile();
		new ZFitsFile("/home/tarrox/physik/fact/data/20130331_013.fits.fz");
	}
}
