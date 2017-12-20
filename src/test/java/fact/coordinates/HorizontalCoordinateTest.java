package fact.coordinates;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import fact.coordinates.Resources.Source;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;

/**
 * Created by maxnoe on 29.06.17.
 */
public class HorizontalCoordinateTest {
    static Logger log = LoggerFactory.getLogger(HorizontalCoordinateTest.class);
    private double precisionAngles = 0.02;
    private double precisionDistance = 0.02;

    @Test
    public void toHorizontalTest() throws Exception {
        InputStream testData = EquatorialCoordinateTest.class.getResourceAsStream("/coordinates/test_data.json");

        Gson gson = new Gson();
        JsonReader jsonReader = new JsonReader(new InputStreamReader(testData));
        Source[] sources = gson.fromJson(jsonReader, Source[].class);

        for (Source source : sources) {
            log.debug("Testing transform for source {} at {}", source.name, source.obstime);

            ZonedDateTime obstime = ZonedDateTime.parse(source.obstime.replace(" ", "T") + "Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            HorizontalCoordinate sourceHz = HorizontalCoordinate.fromDegrees(source.zd, source.az);
            EquatorialCoordinate astropyReference = EquatorialCoordinate.fromHourAngleAndDegrees(source.ra, source.dec);

            EquatorialCoordinate sourceEq = sourceHz.toEquatorial(obstime, EarthLocation.FACT);

            log.debug("date: {}, deviation {}", obstime, astropyReference.greatCircleDistanceDeg(sourceEq));
            assertEquals("greatCircleDistance to reference to large", 0.0, astropyReference.greatCircleDistanceDeg(sourceEq), precisionDistance);
            assertEquals("Difference in RA to large", astropyReference.getRightAscensionDeg(), sourceEq.getRightAscensionDeg(), precisionAngles);
            assertEquals("Difference in Dec to large", astropyReference.getDeclinationDeg(), sourceEq.getDeclinationDeg(), precisionAngles);
        }
    }
}
