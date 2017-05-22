package fact.coordinates;

import fact.FactAnalysisTest;
import fact.features.source.SourcePosition;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class CoordinatesTests {
    //Adjust the precision for the equatorialToHorizontal (raDec to AzimuthZenith) conversion here.
    double precision = 0.35;


    /**
     * Coordinates are compared to those we get from astropy
     * from astropy.time import Time
     * from astropy import units as u
     * from astropy.coordinates import SkyCoord, EarthLocation, AltAz
     * <p>
     * orm = EarthLocation.from_geodetic(
     * lon=-17.891366*u.deg,
     * lat=28.761795*u.deg,
     * height=2200*u.meter,
     * )
     * <p>
     * mrk = SkyCoord.from_name('Mrk421')
     * mrk.ra.hourangle
     * mrk.dec.deg
     * <p>
     * t = Time('2014-03-25 00:42:29', scale='utc', location=orm)
     * frame = AltAz(obstime=t, location=orm)
     * mrk_t = mrk.transform_to(frame)
     * zd = 90 - mrk_t.alt.deg
     * if mrk_t.az.deg > 180:
     * az = mrk_t.az.deg - 360
     * else:
     * az = mrk_t.az.deg
     * print('Az: {} Zd {}'.format(az, zd))
     */
    @Test
    public void testRaDecToAltZenithForMrk421() {
        ZonedDateTime observationTime = ZonedDateTime.of(2014, 3, 25, 00, 42, 29, 0, ZoneOffset.UTC);

        EarthLocation FACTLocation = EarthLocation.fromDegrees(28.761795, -17.890701389, 2200);

        EquatorialCoordinate equatorialCoordinate = EquatorialCoordinate.fromHourAngleAndDegrees(11.074266, 38.208801);
        HorizontalCoordinate horizontalCoordinate = equatorialCoordinate.toHorizontal(observationTime, FACTLocation);

        assertEquals(horizontalCoordinate.getAzimuthDeg(), -35.79439714367527, precision);
        assertEquals(horizontalCoordinate.getZenithDeg(), 11.899209660776094, precision);
    }

    @Test
    public void testRaDecToAltZenithForMrk501() {

        ZonedDateTime observationTime = ZonedDateTime.of(2013, 2, 25, 22, 42, 29, 0, ZoneOffset.UTC);

        EarthLocation FACTLocation = EarthLocation.fromDegrees(28.761795, -17.890701389, 2200);

        EquatorialCoordinate equatorialCoordinate = EquatorialCoordinate.fromHourAngleAndDegrees(16.8978379666666, 39.76016913);
        HorizontalCoordinate horizontalCoordinate = equatorialCoordinate.toHorizontal(observationTime, FACTLocation);

        assertEquals(horizontalCoordinate.getAzimuthDeg(), 33.548727099114615, precision);
        assertEquals(horizontalCoordinate.getZenithDeg(), 99.68421165386194, precision);

    }


    @Test
    public void testRaDecToAltZenithForCrab() {
        ZonedDateTime observationTime = ZonedDateTime.of(2015, 11, 25, 22, 42, 29, 0, ZoneOffset.UTC);
        EarthLocation FACTLocation = EarthLocation.fromDegrees(28.761795, -17.890701389, 2200);

        EquatorialCoordinate equatorialCoordinate = EquatorialCoordinate.fromHourAngleAndDegrees(5.57553886, 22.0145);
        HorizontalCoordinate horizontalCoordinate = equatorialCoordinate.toHorizontal(observationTime, FACTLocation);

        assertEquals(horizontalCoordinate.getAzimuthDeg(), 83.87052808190194, precision);
        assertEquals(horizontalCoordinate.getZenithDeg(), 51.108027267582536, precision);
    }


    @Test
    public void testRaDecToAltZenithFor1ES2344514() {
        ZonedDateTime observationTime = ZonedDateTime.of(2015, 10, 1, 4, 0, 29, 0, ZoneOffset.UTC);
        EarthLocation FACTLocation = EarthLocation.fromDegrees(28.761795, -17.890701389, 2200);

        EquatorialCoordinate equatorialCoordinate = EquatorialCoordinate.fromHourAngleAndDegrees(23.784676866666672, 51.704967);
        HorizontalCoordinate horizontalCoordinate = equatorialCoordinate.toHorizontal(observationTime, FACTLocation);

        assertEquals(horizontalCoordinate.getAzimuthDeg(), -44.335413306125076, precision);
        assertEquals(horizontalCoordinate.getZenithDeg(), 46.337781827425495, precision);
    }

    @Test
    public void testRaDecToAltZenith() {
        // 6th of April in 2004 at 21:00 o clock
        // alt az coordinates from http://www.convertalot.com/celestial_horizon_co-ordinates_calculator.html
        ZonedDateTime observationTime = ZonedDateTime.of(2004, 4, 6, 21, 0, 0, 0, ZoneOffset.UTC);
        EarthLocation FACTLocation = EarthLocation.fromDegrees(28.761795, -17.890701389, 2200);

        EquatorialCoordinate equatorialCoordinate = EquatorialCoordinate.fromHourAngleAndDegrees(3.0, 24.0);
        HorizontalCoordinate horizontalCoordinate = equatorialCoordinate.toHorizontal(observationTime, FACTLocation);

        assertEquals(horizontalCoordinate.getAzimuthDeg(), 290.3377994063796 - 360, precision);
        assertEquals(horizontalCoordinate.getZenithDeg(), 90 - 13.234937521900273, precision);
    }

}
