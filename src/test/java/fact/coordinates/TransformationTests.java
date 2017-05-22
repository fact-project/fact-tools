package fact.coordinates;


import fact.features.source.SourcePosition;
import org.junit.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.Assert.*;

public class TransformationTests {
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
    public void testRaDecRoundTripForMrk421() {
        ZonedDateTime observationTime = ZonedDateTime.of(2014, 3, 25, 00, 42, 29, 0, ZoneOffset.UTC);
        EarthLocation FACTLocation = EarthLocation.fromDegrees(28.761795, -17.890701389, 2200);

        EquatorialCoordinate equatorialCoordinate1 = EquatorialCoordinate.fromHourAngleAndDegrees(11.074266, 38.208801);
        HorizontalCoordinate horizontalCoordinate = equatorialCoordinate1.toHorizontal(observationTime, FACTLocation);
        EquatorialCoordinate equatorialCoordinate2 = horizontalCoordinate.toEquatorial(observationTime, FACTLocation);

        assertEquals("RA", equatorialCoordinate1.getRightAscensionRad(), equatorialCoordinate2.getRightAscensionRad(), 0.01);
        assertEquals("DEC", equatorialCoordinate1.getDeclinationRad(), equatorialCoordinate2.getDeclinationRad(), 0.01);
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



    @Test
    public void testSouthOrientation()
    {
        HorizontalCoordinate sourcePosition = HorizontalCoordinate.fromDegrees(10.6, 0.0);
        HorizontalCoordinate pointingPosition = HorizontalCoordinate.fromDegrees(10.0, 0.0);

        CameraCoordinate cameraCoordinate = sourcePosition.toCamera(pointingPosition, 4.889e3);

        SourcePosition sourcePositionProcessor = new SourcePosition();
        sourcePositionProcessor.setOutputKey("test");
        double[] r = sourcePositionProcessor.getSourcePosition(
                pointingPosition.getAzimuthDeg(), pointingPosition.getZenithDeg(),
                sourcePosition.getAzimuthDeg(), sourcePosition.getZenithDeg()
        );

        assertEquals(r[0], cameraCoordinate.getXMM(), 0.015 );
        assertEquals(r[1], cameraCoordinate.getYMM(), 0.0001 );
    }

    @Test
    public void testHorizontalToCamera1()
    {
        HorizontalCoordinate sourcePosition = HorizontalCoordinate.fromDegrees(10, 0.0);
        HorizontalCoordinate pointingPosition = HorizontalCoordinate.fromDegrees(10.6, 0.0);

        CameraCoordinate cameraCoordinate = sourcePosition.toCamera(pointingPosition, 4.889e3);

        SourcePosition sourcePositionProcessor = new SourcePosition();
        sourcePositionProcessor.setOutputKey("test");
        double[] r = sourcePositionProcessor.getSourcePosition(
                pointingPosition.getAzimuthDeg(), pointingPosition.getZenithDeg(),
                sourcePosition.getAzimuthDeg(), sourcePosition.getZenithDeg()
        );

        assertEquals(r[0], cameraCoordinate.getXMM(), 0.015 );
        assertEquals(r[1], cameraCoordinate.getYMM(), 0.0001 );
    }

    @Test
    public void testHorizontalToCamera2()
    {

        HorizontalCoordinate sourcePosition = HorizontalCoordinate.fromDegrees(90.0, 0.6);
        HorizontalCoordinate pointingPosition = HorizontalCoordinate.fromDegrees(90.0, 0.0);

        CameraCoordinate cameraCoordinate = sourcePosition.toCamera(pointingPosition, 4.889e3);

        SourcePosition sourcePositionProcessor = new SourcePosition();
        sourcePositionProcessor.setOutputKey("test");
        double[] r = sourcePositionProcessor.getSourcePosition(
                pointingPosition.getAzimuthDeg(), pointingPosition.getZenithDeg(),
                sourcePosition.getAzimuthDeg(), sourcePosition.getZenithDeg()
        );

        assertEquals(r[0], cameraCoordinate.getXMM(), 0.0001 );
        assertEquals(r[1], cameraCoordinate.getYMM(), 0.015 );
    }

    @Test
    public void testHorizontalToCamera3()
    {

        HorizontalCoordinate sourcePosition = HorizontalCoordinate.fromDegrees(10, 45);
        HorizontalCoordinate pointingPosition = HorizontalCoordinate.fromDegrees(11, 40);

        CameraCoordinate cameraCoordinate = sourcePosition.toCamera(pointingPosition, 4.889e3);

        SourcePosition sourcePositionProcessor = new SourcePosition();
        sourcePositionProcessor.setOutputKey("test");
        double[] r = sourcePositionProcessor.getSourcePosition(
                pointingPosition.getAzimuthDeg(), pointingPosition.getZenithDeg(),
                sourcePosition.getAzimuthDeg(), sourcePosition.getZenithDeg()
        );

        assertEquals(r[0], cameraCoordinate.getXMM(), 0.0001 );
        assertEquals(r[1], cameraCoordinate.getYMM(), 0.015 );
    }

    @Test
    public void testCameraToHHorizontal() {

        HorizontalCoordinate sourcePosition = HorizontalCoordinate.fromDegrees(10, 45);
        HorizontalCoordinate pointingPosition = HorizontalCoordinate.fromDegrees(11, 40);

        CameraCoordinate cameraCoordinate = sourcePosition.toCamera(pointingPosition, 4.889e3);
        HorizontalCoordinate trafoPosition = cameraCoordinate.toHorizontal(pointingPosition, 4.889e3);

        assertEquals(trafoPosition.getAzimuthRad(), sourcePosition.getAzimuthRad(), 0.0001);
        assertEquals(trafoPosition.getZenithRad(), sourcePosition.getZenithRad(), 0.0001);
    }

    @Test
    public void testRoundTripMrk421() {
        ZonedDateTime observationTime = ZonedDateTime.of(2014, 3, 25, 00, 42, 29, 0, ZoneOffset.UTC);
        EarthLocation FACTLocation = EarthLocation.fromDegrees(28.761795, -17.890701389, 2200);
        HorizontalCoordinate pointingPosition = HorizontalCoordinate.fromDegrees(11.0, -35.0);

        EquatorialCoordinate mrk421Equatorial = EquatorialCoordinate.fromHourAngleAndDegrees(11.074266, 38.208801);

        HorizontalCoordinate mrk421Horizontal = mrk421Equatorial.toHorizontal(observationTime, FACTLocation);
        CameraCoordinate mrk421Camera = mrk421Horizontal.toCamera(pointingPosition, 4.889e3);
        HorizontalCoordinate mrk421HorizontalBack = mrk421Camera.toHorizontal(pointingPosition, 4.889e3);
        EquatorialCoordinate mrk421EquatorialBack = mrk421HorizontalBack.toEquatorial(observationTime, FACTLocation);

        assertEquals(0.0, mrk421HorizontalBack.greatCircleDistance(mrk421Horizontal),   1e-12);
        assertEquals(0.0, mrk421EquatorialBack.greatCircleDistance(mrk421Equatorial), 1e-12);
    }

    @Test
    public void testRoundTripMrk501() {
        ZonedDateTime observationTime = ZonedDateTime.of(2013, 2, 25, 22, 42, 29, 0, ZoneOffset.UTC);
        EarthLocation FACTLocation = EarthLocation.fromDegrees(28.761795, -17.890701389, 2200);
        HorizontalCoordinate pointingPosition = HorizontalCoordinate.fromDegrees(99.0, 33.0);

        EquatorialCoordinate mrk501Equatorial = EquatorialCoordinate.fromHourAngleAndDegrees(16.8978379666666, 39.76016913);

        HorizontalCoordinate mrk501Horizontal = mrk501Equatorial.toHorizontal(observationTime, FACTLocation);
        CameraCoordinate mrk501Camera = mrk501Horizontal.toCamera(pointingPosition, 4.889e3);
        HorizontalCoordinate mrk501HorizontalBack = mrk501Camera.toHorizontal(pointingPosition, 4.889e3);
        EquatorialCoordinate mrk501EquatorialBack = mrk501HorizontalBack.toEquatorial(observationTime, FACTLocation);

        assertEquals(0.0, mrk501HorizontalBack.greatCircleDistance(mrk501Horizontal), 1e-12);
        assertEquals(0.0, mrk501EquatorialBack.greatCircleDistance(mrk501Equatorial), 1e-12);
    }

    @Test
    public void testRoundTripCrab() {
        ZonedDateTime observationTime = ZonedDateTime.of(2015, 11, 25, 22, 42, 29, 0, ZoneOffset.UTC);
        EarthLocation FACTLocation = EarthLocation.fromDegrees(28.761795, -17.890701389, 2200);
        HorizontalCoordinate pointingPosition = HorizontalCoordinate.fromDegrees(52.0, 85.0);

        EquatorialCoordinate crabEquatorial = EquatorialCoordinate.fromHourAngleAndDegrees(5.57553886, 22.0145);

        HorizontalCoordinate crabHorizontal = crabEquatorial.toHorizontal(observationTime, FACTLocation);
        CameraCoordinate crabCamera = crabHorizontal.toCamera(pointingPosition, 4.889e3);
        HorizontalCoordinate crabHorizontalBack = crabCamera.toHorizontal(pointingPosition, 4.889e3);
        EquatorialCoordinate crabEquatorialBack = crabHorizontalBack.toEquatorial(observationTime, FACTLocation);

        assertEquals(0.0, crabHorizontalBack.greatCircleDistance(crabHorizontal), 1e-12);
        assertEquals(0.0, crabEquatorialBack.greatCircleDistance(crabEquatorial), 1e-12);
    }

}
