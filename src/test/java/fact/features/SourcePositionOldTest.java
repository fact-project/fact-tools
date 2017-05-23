package fact.features;

import fact.features.source.SourcePositionOld;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class SourcePositionOldTest {
    static Logger log = LoggerFactory.getLogger(SourcePositionOldTest.class);

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
     zd = 90 - mrk_t.alt.deg
     if mrk_t.az.deg > 180:
        az = mrk_t.az.deg - 360
     else:
        az = mrk_t.az.deg
     print('Az: {} Zd {}'.format(az, zd))
     *
     *
     */
    @Test
    public void testRaDecToAltZenithForMrk421(){
        SourcePositionOld sourcePositionOld = new SourcePositionOld();
        DateTime dateTime = new DateTime(2014, 3, 25, 00, 42, 29, DateTimeZone.UTC);
        double ra = 11.074266;
        double dec = 38.208801;
        double[] azAlt = sourcePositionOld.equatorialToHorizontal(ra, dec, dateTime);
        assertEquals(azAlt[0], -35.79439714367527, precision);
        assertEquals(azAlt[1], 11.899209660776094, precision);
    }


    @Test
    public void testRaDecToAltZenithForMrk501(){
        SourcePositionOld sourcePositionOld = new SourcePositionOld();
        DateTime dateTime = new DateTime(2013, 2, 25, 22, 42, 29, DateTimeZone.UTC);
        double ra = 16.8978379666666;
        double dec = 39.76016913;
        double[] azAlt = sourcePositionOld.equatorialToHorizontal(ra, dec, dateTime);
        assertEquals(azAlt[0], 33.548727099114615, precision);
        assertEquals(azAlt[1], 99.68421165386194, precision);
    }

    @Test
    public void testRaDecToAltZenithForCrab(){
        SourcePositionOld sourcePositionOld = new SourcePositionOld();
        DateTime dateTime = new DateTime(2015, 11, 25, 22, 42, 29, DateTimeZone.UTC);
        double ra = 5.57553886;
        double dec = 22.0145;
        double[] azAlt = sourcePositionOld.equatorialToHorizontal(ra, dec, dateTime);
        assertEquals(azAlt[0], 83.87052808190194, precision);
        assertEquals(azAlt[1], 51.108027267582536, precision);
    }

    @Test
    public void testRaDecToAltZenithFor1ES2344514(){
        SourcePositionOld sourcePositionOld = new SourcePositionOld();
        DateTime dateTime = new DateTime(2015, 10, 01, 04, 00, 29, DateTimeZone.UTC);
        double ra = 23.784676866666672;
        double dec = 51.704967;
        double[] azAlt = sourcePositionOld.equatorialToHorizontal(ra, dec, dateTime);
        assertEquals(azAlt[0], -44.335413306125076, precision);
        assertEquals(azAlt[1], 46.337781827425495, precision);
    }


    @Test
    public void testRaDecToAltZenith(){
        // 6th of April in 2004 at 21:00 o clock
        // alt az coordinates from http://www.convertalot.com/celestial_horizon_co-ordinates_calculator.html
        SourcePositionOld sourcePositionOld = new SourcePositionOld();
        DateTime dateTime = new DateTime(2004, 4, 6, 21, 0, 0, DateTimeZone.UTC);
        double ra = 3.0;
        double dec = 24.0;
        double[] azAlt = sourcePositionOld.equatorialToHorizontal(ra, dec, dateTime);
        assertEquals(azAlt[0], 290.3377994063796 - 360, precision);
        assertEquals(azAlt[1], 90 - 13.234937521900273 , precision);
    }







    @Test
    public void testWobbleCorrectParameter() throws Exception {
        SourcePositionOld sourcePositionOld = new SourcePositionOld();
        sourcePositionOld.setOutputKey("test");

        sourcePositionOld.setPointingAzKey("hello");
        sourcePositionOld.setPointingZdKey("I am");
        sourcePositionOld.setSourceAzKey("quite the ");
        sourcePositionOld.setSourceZdKey("annoying feature.");
        sourcePositionOld.init(null);

        assertThat(sourcePositionOld.hasMcWobblePosition, is(true));
    }


    @Test
    public void testWobbleWrongParameter() throws Exception {
        SourcePositionOld sourcePositionOld = new SourcePositionOld();
        sourcePositionOld.setOutputKey("test");

        //set only one key. eventhough we need all 4
        sourcePositionOld.setPointingAzKey("bla");
        try {
            sourcePositionOld.init(null);
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("Should have caught an IllegalArgumentException here.");
    }


	@Test
	public void testSouthOrientation()
	{
        SourcePositionOld sourcePositionOld = new SourcePositionOld();
        sourcePositionOld.setOutputKey("test");

		double zdSource = 0.6;
		double azSource = 0.0;
		double zdPointing = 0.0;
		double azPointing = 0.0;

		double[] r = sourcePositionOld.getSourcePosition(azPointing, zdPointing, azSource, zdSource);
		assertEquals("Calculated position for source in (zd=0.6°,az=0.0) for pointing in(zd=0.0°,az=0.0) is wrong " +
                "Calculated position: (\"+r[0]+\",\"+r[1]+\") \n" +
                "sought position: (51.20983219603325,-0.0)", 51.20983219603325, r[0], 0.015 );
        assertEquals("Calculated position for source in (zd=0.6°,az=0.0) for pointing in(zd=0.0°,az=0.0) is wrong " +
                "Calculated position: (\"+r[0]+\",\"+r[1]+\") \n" +
                "sought position: (51.20983219603325,-0.0)", 0.0, r[1], 0.0001 );
	}
}
