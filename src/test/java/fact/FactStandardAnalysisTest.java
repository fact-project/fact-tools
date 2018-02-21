package fact;

import fact.io.hdureader.BinTable;
import fact.io.hdureader.FITS;
import org.junit.Test;

import java.io.File;

import static fact.RunFACTTools.runFACTTools;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class FactStandardAnalysisTest {


    @Test
    public void dataTest() {

        try {
            File xml = new File("examples/stdAnalysis.xml");
            File outFile = File.createTempFile("facttools_test", ".fits");

            String[] args = {
                    xml.toURI().toString(),
                    "-Dinfile=classpath:/testDataFile.fits.gz",
                    "-Ddrsfile=classpath:/testDrsFile.drs.fits.gz",
                    "-DauxFolder=file:src/main/resources/aux/",
                    "-Doutfile=" + outFile.toURI().toString(),
            };

            runFACTTools(args);

            FITS fits = FITS.fromFile(outFile);
            BinTable table = fits.getBinTableByName("Events").get();

            assertEquals(3, (long) table.numberOfRowsInTable);


        } catch (Exception e) {
            e.printStackTrace();
            fail("Could not run examples/stdAnalysis.xml for observations");
        }
    }


    @Test
    public void mcTest() {

        try {
            File xml = new File("examples/stdAnalysis.xml");
            File outFile = File.createTempFile("facttools_test", ".fits");

            String[] args = {
                    xml.toURI().toString(),
                    "-Doutfile=" + outFile.toURI().toString(),
                    "-Dinfile=file:src/main/resources/testMcFile.fits.gz",
                    "-Ddrsfile=file:src/main/resources/testMcDrsFile.drs.fits.gz",
                    "-DintegralGainFile=classpath:/default/defaultIntegralGains.csv",
                    "-DpixelDelayFile=classpath:/default/delays_zero.csv",
            };

            runFACTTools(args);

            FITS fits = FITS.fromFile(outFile);
            BinTable table = fits.getBinTableByName("Events").get();

            assertEquals(13, (long) table.numberOfRowsInTable);


        } catch (Exception e) {
            e.printStackTrace();
            fail("Could not run examples/stdAnalysis/simulations.xml");
        }
    }
}

