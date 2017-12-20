package fact.io.hdureader;

import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * Test reading some reading of aux files
 * Created by mackaiver on 19/12/16.
 */
public class ReadAuxFiles {
    @Test
    public void testBinTableIteratorForMCs() throws Exception {
        URL u = ReadAuxFiles.class.getResource("/20130102.DRIVE_CONTROL_SOURCE_POSITION.fits");

        FITS f = new FITS(u);

        BinTable b = f.getBinTableByName("DRIVE_CONTROL_SOURCE_POSITION").orElseThrow(IOException::new);

        for (OptionalTypesMap p : BinTableReader.forBinTable(b)) {
            assertTrue(p.containsKey("Ra_src"));
            assertTrue(p.containsKey("Dec_src"));
            assertTrue(p.containsKey("Name"));
        }

    }

    @Test
    public void testBinTableReader() throws Exception {
        URL u = ReadAuxFiles.class.getResource("/20130102.DRIVE_CONTROL_SOURCE_POSITION.fits");

        FITS f = new FITS(u);

        BinTable b = f.getBinTableByName("DRIVE_CONTROL_SOURCE_POSITION").orElseThrow(IOException::new);
        BinTableReader reader = BinTableReader.forBinTable(b);

        //first row contains null stuff
        Map<String, Serializable> row = reader.getNextRow();
        double ra = (double) row.get("Ra_src");
        double dec = (double) row.get("Dec_src");
        String name = (String) row.get("Name");
        assertThat(ra, is(0.0));
        assertThat(dec, is(0.0));
        assertEquals(name, "");

        //check position of crab which is at 5.5h,  22.0deg
        row = reader.getNextRow();
        ra = (double) row.get("Ra_src");
        dec = (double) row.get("Dec_src");
        name = (String) row.get("Name");

        assertEquals(5.5, ra, 0.1);
        assertEquals(22.0, dec, 0.1);
        assertEquals("Crab", name);

    }
}
