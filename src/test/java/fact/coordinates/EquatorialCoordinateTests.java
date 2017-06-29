package fact.coordinates;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;

import fact.coordinates.Resources.Source;

/* Python code used to get astropy references

from argparse import ArgumentParser
from astropy.time import Time
from astropy import units as u
from astropy.coordinates import SkyCoord, EarthLocation, AltAz

parser = ArgumentParser()
parser.add_argument('source')
parser.add_argument('timestamp')

if __name__ == '__main__':
    args = parser.parse_args()

    orm = EarthLocation.from_geodetic(
        lon=-17.891366 * u.deg,
        lat=28.761795 * u.deg,
        height=2200 * u.meter,
    )

    source = SkyCoord.from_name(args.source)

    t = Time(args.timestamp, scale='utc')
    frame = AltAz(obstime=t, location=orm)
    source_t = source.transform_to(frame)

    if source_t.az.deg > 180:
        az = source_t.az.deg - 360
    else:
        az = source_t.az.deg

    zd = source_t.zen.deg

    print('Time: "', t.iso, '"', sep='')
    print('Ra: {} h, Dec: {}°'.format(source.ra.hourangle, source.dec.deg))
    print('Zd: {} °, Az: {} °'.format(az, zd))

*/


/**
 * Created by maxnoe on 29.06.17
 */
public class EquatorialCoordinateTests {

    static Logger log = LoggerFactory.getLogger(EquatorialCoordinateTests.class);
    private double precisionAngles = 0.25;
    private double precisionDistance = 0.005;

    @Test
    public void toHorizontalTest() throws Exception {
        InputStream testData = EquatorialCoordinateTests.class.getResourceAsStream("/coordinates/test_data.json");

        Gson gson = new Gson();
        JsonReader jsonReader = new JsonReader(new InputStreamReader(testData));
        Source[] sources = gson.fromJson(jsonReader, Source[].class);

        for(Source source: sources){
            log.info("Testing transform for source {} at {}", source.name, source.obstime);
            ZonedDateTime obstime = ZonedDateTime.parse(source.obstime.replace(" ", "T") + "Z[UTC]");
            EquatorialCoordinate sourceEq = EquatorialCoordinate.fromHourAngleAndDegrees(source.ra, source.dec);
            HorizontalCoordinate astropyReference = HorizontalCoordinate.fromDegrees(source.zd, source.az);

            HorizontalCoordinate sourceHz = sourceEq.toHorizontal(obstime, EarthLocation.FACT);

            assertEquals(0.0, astropyReference.greatCircleDistance(sourceHz), precisionDistance);
            assertEquals(astropyReference.getAzimuthDeg(), sourceHz.getAzimuthDeg(), precisionAngles);
            assertEquals(astropyReference.getZenithDeg(), sourceHz.getZenithDeg(), precisionAngles);
        }
    }
}