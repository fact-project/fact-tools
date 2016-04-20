package fact.features;

import fact.Constants;
import fact.features.source.SourcePosition;
import fact.hexmap.FactPixelMapping;
import junit.framework.Assert;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class SourcePositionTest {
    static Logger log = LoggerFactory.getLogger(SourcePositionTest.class);

    //Adjust the precision for the equatorialToHorizontal (raDec to AzimuthZenith) conversion here.
    double precision = 0.35;


    /**
     * Coordinates are compared to those we get from astropy
     from astropy.time import Time
     from astropy import units as u
     from astropy.coordinates import SkyCoord, EarthLocation, AltAz

     orm = EarthLocation.from_geodetic(
         lon=-17.891366*u.deg,
         lat=28.761795*u.deg,
         height=2200*u.meter,
     )

     mrk = SkyCoord.from_name('Mrk421')
     mrk.ra.hourangle
     mrk.dec.deg

     t = Time('2014-03-25 00:42:29', scale='utc', location=orm)
     frame = AltAz(obstime=t, location=orm)
     mrk_t = mrk.transform_to(frame)
     print('Az Alt')
     mrk_t.az.deg, 90 - mrk_t.alt.deg
     *
     *
     */
    @Test
    public void testRaDecToAltZenithForMrk421(){
        SourcePosition sourcePosition = new SourcePosition();
        DateTime dateTime = new DateTime(2014, 3, 25, 00, 42, 29, DateTimeZone.UTC);
        double ra = 11.074266;
        double dec = 38.208801;
        double[] azAlt = sourcePosition.equatorialToHorizontal(ra, dec, dateTime);
        assertEquals(azAlt[0], -35.79439714367527, precision);
        assertEquals(azAlt[1], 11.899209660776094, precision);
    }


    @Test
    public void testRaDecToAltZenithForMrk501(){
        SourcePosition sourcePosition = new SourcePosition();
        DateTime dateTime = new DateTime(2013, 2, 25, 22, 42, 29, DateTimeZone.UTC);
        double ra = 16.8978379666666;
        double dec = 39.76016913;
        double[] azAlt = sourcePosition.equatorialToHorizontal(ra, dec, dateTime);
        assertEquals(azAlt[0], -326.45127290084133, precision);
        assertEquals(azAlt[1], 99.68421165386194, precision);
    }

    @Test
    public void testRaDecToAltZenithForCrab(){
        SourcePosition sourcePosition = new SourcePosition();
        DateTime dateTime = new DateTime(2015, 11, 25, 22, 42, 29, DateTimeZone.UTC);
        double ra = 5.57553886;
        double dec = 22.0145;
        double[] azAlt = sourcePosition.equatorialToHorizontal(ra, dec, dateTime);
        assertEquals(azAlt[0], -276.12947205268705, precision);
        assertEquals(azAlt[1], 51.108027267582536, precision);
    }

    @Test
    public void testRaDecToAltZenithFor1ES2344514(){
        SourcePosition sourcePosition = new SourcePosition();
        DateTime dateTime = new DateTime(2015, 10, 01, 04, 00, 29, DateTimeZone.UTC);
        double ra = 23.784676866666672;
        double dec = 51.704967;
        double[] azAlt = sourcePosition.equatorialToHorizontal(ra, dec, dateTime);
        assertEquals(azAlt[0], -44.335413306125076, precision);
        assertEquals(azAlt[1], 46.337781827425495, precision);
    }


    @Test
    public void testRaDecToAltZenith(){
        // 6th of April in 2004 at 21:00 o clock
        // alt az coordinates from http://www.convertalot.com/celestial_horizon_co-ordinates_calculator.html
        SourcePosition sourcePosition = new SourcePosition();
        DateTime dateTime = new DateTime(2004, 4, 6, 21, 0, 0, DateTimeZone.UTC);
        double ra = 3.0;
        double dec = 24.0;
        double[] azAlt = sourcePosition.equatorialToHorizontal(ra, dec, dateTime);
        assertEquals(azAlt[0], 290.3377994063796 - 360, precision);
        assertEquals(azAlt[1], 90 - 13.234937521900273 , precision);
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


    /**
     *
     * For some known az, dec coordinates for Ceta Tauri calculate the corresponding sourceposition
     * in the camera and compare that to the position of the pixels which are known to show the image of ceta tauri
     *
     * @throws Exception
     */

	/*
	@Test
	public void testCetaTauri() throws Exception
	{
        SourcePosition sourcePosition = new SourcePosition();
        sourcePosition.setOutputKey("test");

		double C_T_rightAscension = (5.0 + 37.0/60 + 38.7/3600);
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
		
		double pointingRa = 83.1375/360 * 24;
		double pointingDec = 21.628055555555555;
		double gmst = 1.1289573103059787;
		
		double[] pointingAzDe = sourcePosition.equatorialToHorizontal(pointingRa, pointingDec, gmst);
		double[] sourceAzDe = sourcePosition.equatorialToHorizontal(C_T_rightAscension, C_T_declination, gmst);
		
		double[] sPos =  sourcePosition.getSourcePosition(pointingAzDe[0], pointingAzDe[1], sourceAzDe[0], sourceAzDe[1]);
				
		assertTrue("Calculated coordinates of ceta tauri doesn't fit the coordinates I got from flashy pixels:\n"
				+ "Coord_from_pixel: ("+C_T_coord[0]+","+C_T_coord[1]+")\n"
				+ "Coord_calculated: ("+sPos[0]+","+sPos[1]+")", (Math.abs(C_T_coord[0]-sPos[0])<Constants.PIXEL_SIZE && Math.abs(C_T_coord[1]-sPos[1])<Constants.PIXEL_SIZE));
	}
	*/
	
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
