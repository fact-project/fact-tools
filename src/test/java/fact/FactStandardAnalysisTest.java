package fact;

import fact.io.FITSStreamTest;
import fact.io.hdureader.BinTable;
import fact.io.hdureader.BinTableReader;
import fact.io.hdureader.FITS;
import fact.io.hdureader.OptionalTypesMap;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class FactStandardAnalysisTest {


	@Test
	public void dataTest() {

		try {
			File xml = new File("examples/stdAnalysis/data/analysis.xml");
			File outFile = File.createTempFile("facttools_test", ".fits");

            String[] args = {xml.toURI().toString(), "-Doutfile=" + outFile.toURI().toString()};

			stream.run.main(args);

            FITS fits = FITS.fromFile(outFile);
            BinTable table = fits.getBinTableByName("Events").get();

            assertEquals(3, (long) table.numberOfRowsInTable);


		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not run the ./fact_example.xml");
		}
	}


    @Test
    public void mcTest() {

        try {
            File xml = new File("examples/stdAnalysis/mc/analysis_mc.xml");
            File outFile = File.createTempFile("facttools_test", ".fits");

            String[] args = {xml.toURI().toString(), "-Doutfile=" + outFile.toURI().toString()};

            stream.run.main(args);

            FITS fits = FITS.fromFile(outFile);
            BinTable table = fits.getBinTableByName("Events").get();

            assertEquals(13, (long) table.numberOfRowsInTable);


        } catch (Exception e) {
            e.printStackTrace();
            fail("Could not run the ./fact_example.xml");
        }
    }
}

