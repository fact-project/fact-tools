package fact.coordinates;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;

/**
 * Created by maxnoe on 29.06.17.
 */
public class CelestialCoordinateTest {

    static Logger log = LoggerFactory.getLogger(CelestialCoordinate.class);
    private double precision = 1e-4;

    @Test
    public void testGMST() {

        InputStream testDataStream = EquatorialCoordinateTest.class.getResourceAsStream("/coordinates/gmst_test_data.json");

        Gson gson = new Gson();
        JsonReader jsonReader = new JsonReader(new InputStreamReader(testDataStream));
        Resources.GMSTData[] testData = gson.fromJson(jsonReader, Resources.GMSTData[].class);

        for (Resources.GMSTData gmstData : testData) {
            log.debug("Testing GMST for {}", gmstData.obstime);
            ZonedDateTime obstime = ZonedDateTime.parse(gmstData.obstime.replace(" ", "T") + "Z[UTC]");
            double gmst = CelestialCoordinate.datetimeToGST(obstime);
            assertEquals(gmstData.gmst_rad, gmst, precision);
            log.debug("Deviation: {}", gmst - gmstData.gmst_rad);
        }

    }
}
