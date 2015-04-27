package fact.features;

import fact.Constants;
import fact.features.source.SourcePosition;
import fact.hexmap.FactPixelMapping;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SourcePositionTest {
    static Logger log = LoggerFactory.getLogger(SourcePositionTest.class);

    FactPixelMapping pixelMap = FactPixelMapping.getInstance();

    @Test
    public void testWobbleWrongParameter() throws Exception {
        SourcePosition sourcePosition = new SourcePosition();
        sourcePosition.setOutputKey("test");

        //set only one key. eventhough we need all 4
        sourcePosition.setPointingAzKey("bla");
        try {
            sourcePosition.init(null);
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("Should have caught an IllegalArgumentException here.");

    }


    @Test
    public void testWobbleCorrectParameter() throws Exception {
        SourcePosition sourcePosition = new SourcePosition();
        sourcePosition.setOutputKey("test");

        sourcePosition.setPointingAzKey("hello");
        sourcePosition.setPointingZdKey("I am");
        sourcePosition.setSourceAzKey("quite the ");
        sourcePosition.setSourceZdKey("annoying feature.");
        sourcePosition.init(null);

        assertThat(sourcePosition.hasMcWobblePosition, is(true));
    }


    @Test
    public void testTimeConversion() throws ParseException {
        SourcePosition sourcePosition = new SourcePosition();
        sourcePosition.setOutputKey("test");
        //get a test date and time
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = isoFormat.parse("2014-10-01T16:34:00");

        //get julian day. see http://aa.usno.navy.mil/data/docs/JulianDate.php for an online calculator
        double unixTime = (double) (date.getTime()/1000L);
        double jd = sourcePosition.unixTimeToJulianDay(unixTime);
        Assert.assertEquals(2456932.1902, jd, 0.0001);


        //convert julian day to gmst. See http://koti.mbnet.fi/jukaukor/star_altitude.html for checks.
        double gmst = sourcePosition.julianDayToGmst(jd);
        //convert degrees to hours
        gmst /= Math.PI/12;
        //should be 17 hours 15 minutes and 19.3 seconds
        Assert.assertEquals(17.2553690, gmst, 0.000001);


        date = isoFormat.parse("2014-10-02T00:01:00");
        jd = sourcePosition.unixTimeToJulianDay((int) (date.getTime()/1000));
        gmst = sourcePosition.julianDayToGmst(jd);
        gmst /= Math.PI/12;
        Assert.assertEquals(0.7257664, gmst, 0.000001);


        date = isoFormat.parse("2014-10-01T23:59:59");
        jd = sourcePosition.unixTimeToJulianDay((int) (date.getTime()/1000));
        gmst = sourcePosition.julianDayToGmst(jd);
        gmst /= Math.PI/12;
        Assert.assertEquals(0.708775, gmst, 0.000001);

    }

    /**
     *
     * For some known az, dec coordinates for Ceta Tauri calculate the corresponding sourceposition
     * in the camera and compare that to the position of the pixels which are known to show the image of ceta tauri
     *
     * @throws Exception
     */
	@Test
	public void testCetaTauri() throws Exception
	{
        SourcePosition sourcePosition = new SourcePosition();
        sourcePosition.setOutputKey("test");

		double C_T_rightAscension = (5.0 + 37.0/60 + 38.7/3600) / 24.0 * 360.0;
		double C_T_declination = 21.0 + 8.0/60 + 33.0/3600;
		
		int[] C_T_chids = {1378,215,212};
		double[] C_T_coord = new double[2];
		
		for (int i = 0 ; i < 3 ; i++)
		{
			C_T_coord[0] += pixelMap.getPixelFromId(C_T_chids[i]).getXPositionInMM();
			C_T_coord[1] += pixelMap.getPixelFromId(C_T_chids[i]).getYPositionInMM();
		}
		
		C_T_coord[0] /= 3;
		C_T_coord[1] /= 3;
		
		double pointingRa = 83.1375;
		double pointingDec = 21.628055555555555;
		double gmst = 1.1289573103059787;
		
		double[] pointingAzDe = sourcePosition.getAzZd(pointingRa, pointingDec, gmst);
		double[] sourceAzDe = sourcePosition.getAzZd(C_T_rightAscension, C_T_declination, gmst);
		
		double[] sPos =  sourcePosition.getSourcePosition(pointingAzDe[0], pointingAzDe[1], sourceAzDe[0], sourceAzDe[1]);
				
		assertTrue("Calculated coordinates of ceta tauri doesn't fit the coordinates I got from flashy pixels:\n"
				+ "Coord_from_pixel: ("+C_T_coord[0]+","+C_T_coord[1]+")\n"
				+ "Coord_calculated: ("+sPos[0]+","+sPos[1]+")", (Math.abs(C_T_coord[0]-sPos[0])<Constants.PIXEL_SIZE && Math.abs(C_T_coord[1]-sPos[1])<Constants.PIXEL_SIZE));
	}
	
	@Test
	public void testSouthOrientation()
	{
        SourcePosition sourcePosition = new SourcePosition();
        sourcePosition.setOutputKey("test");

		double zdSource = 0.6;
		double azSource = 0.0;
		double zdPointing = 0.0;
		double azPointing = 0.0;
		
		double[] r = sourcePosition.getSourcePosition(azPointing, zdPointing, azSource, zdSource);
		
		assertTrue("Calculated position for source in (zd=0.6°,az=0.0) for pointing in(zd=0.0°,az=0.0) is wrong\n"
				+ "Calculated position: ("+r[0]+","+r[1]+")\n"
				+ "sought position: (51.20983219603325,-0.0)", (Math.abs(r[0]-51.20983219603325)<1E-14 && Math.abs(r[1]-0.0)<1E-14));		
	}
}
