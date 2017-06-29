package fact.coordinates;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import fact.coordinates.Resources.Source;
import static org.junit.Assert.assertEquals;

/**
 * Created by maxnoe on 29.06.17.
 */
public class HorizontalCoordinateTests {
    static Logger log = LoggerFactory.getLogger(HorizontalCoordinateTests.class);
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
            HorizontalCoordinate sourceHz = HorizontalCoordinate.fromDegrees(source.zd, source.az);
            EquatorialCoordinate astropyReference = EquatorialCoordinate.fromHourAngleAndDegrees(source.ra, source.dec);

            EquatorialCoordinate sourceEq = sourceHz.toEquatorial(obstime, EarthLocation.FACT);

            assertEquals(0.0, astropyReference.greatCircleDistance(sourceEq), precisionDistance);
            assertEquals(astropyReference.getRightAscensionHA(), sourceEq.getRightAscensionHA(), precisionAngles);
            assertEquals(astropyReference.getDeclinationDeg(), sourceEq.getDeclinationDeg(), precisionAngles);
        }
    }
}
