package fact.io.zfits;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.io.SourceURL;
import stream.util.parser.ParseException;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;

public class FitsHeaderTest {
	static Logger log = LoggerFactory.getLogger(FitsHeaderTest.class);

    List<String> headerStrings = new ArrayList<>();

    @Before
    public void setup(){
        headerStrings.add("XTENSION= 'BINTABLE'           / binary table extension  ");
        headerStrings.add("NAXIS   =                    2 / 2-dimensional binary table ");
        headerStrings.add("DATASUM = '3570345674'         / Checksum for the data block  ");
    }

    /**
     * Test whether gzipped fits files containing raw data can be read correctly
     * @throws Exception
     */
	@Test
	public void testReadFits() throws Exception {
        FitsHeader header = new FitsHeader(headerStrings);
        Assert.assertTrue(header.check("XTENSION",FitsHeader.ValueType.STRING));
        Assert.assertTrue(header.check("NAXIS",FitsHeader.ValueType.INT));
        Assert.assertTrue(header.check("DATASUM",FitsHeader.ValueType.STRING));
	}

    @Test
    public void testTypeFromValue() throws ParseException {
        FitsHeader header = new FitsHeader(headerStrings);
        Assert.assertThat(header.getTypeFromValueString("'Test'"), is(FitsHeader.ValueType.STRING));
        Assert.assertThat(header.getTypeFromValueString("-1234234"), is(FitsHeader.ValueType.INT));
        Assert.assertThat(header.getTypeFromValueString("34234"), is(FitsHeader.ValueType.INT));
        Assert.assertThat(header.getTypeFromValueString("+34234"), is(FitsHeader.ValueType.INT));

        Assert.assertThat(header.getTypeFromValueString("2.02342304"), is(FitsHeader.ValueType.FLOAT));
        Assert.assertThat(header.getTypeFromValueString("2.023423E04"), is(FitsHeader.ValueType.FLOAT));
        Assert.assertThat(header.getTypeFromValueString("T"), is(FitsHeader.ValueType.BOOLEAN));
        Assert.assertThat(header.getTypeFromValueString("F"), is(FitsHeader.ValueType.BOOLEAN));
    }
}
